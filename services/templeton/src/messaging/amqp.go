package messaging

import (
	"logcabin"
	"math/rand"
	"os"
	"time"

	"github.com/streadway/amqp"
)

var logger = logcabin.New()

// MessageHandler defines a type for amqp.Delivery handlers.
type MessageHandler func(amqp.Delivery)

type aggregationMessage struct {
	handler  MessageHandler
	delivery *amqp.Delivery
}

type consumer struct {
	exchange string
	queue    string
	key      string
	handler  MessageHandler
}

type publisher struct {
	exchange string
	channel  *amqp.Channel
}

// Client encapsulates the information needed to interact via AMQP.
type Client struct {
	uri             string
	connection      *amqp.Connection
	aggregationChan chan aggregationMessage
	errors          chan *amqp.Error
	consumers       []*consumer
	publisher       *publisher
	Reconnect       bool
}

// NewClient returns a new *Client. It will block until the connection succeeds.
func NewClient(uri string, reconnect bool) (*Client, error) {
	c := &Client{}
	randomizer := rand.New(rand.NewSource(time.Now().UnixNano()))
	c.uri = uri
	c.Reconnect = reconnect
	logger.Println("Attempting AMQP connection...")
	var connection *amqp.Connection
	var err error
	if c.Reconnect {
		for {
			connection, err = amqp.Dial(c.uri)
			if err != nil {
				logger.Print(err)
				waitFor := randomizer.Intn(10)
				logger.Printf("Re-attempting connection in %d seconds", waitFor)
				time.Sleep(time.Duration(waitFor) * time.Second)
			} else {
				logger.Println("Successfully connected to the AMQP broker")
				break
			}
		}
	} else {
		connection, err = amqp.Dial(c.uri)
		if err != nil {
			return nil, err
		}
		logger.Println("Successfully connected to the AMQP broker")
	}
	c.connection = connection
	c.aggregationChan = make(chan aggregationMessage)
	c.errors = c.connection.NotifyClose(make(chan *amqp.Error))
	return c, nil
}

// Listen will wait for messages and pass them off to handlers, which run in
// their own goroutine.
func (c *Client) Listen() {
	var consumers []*consumer
	init := func() {
		for _, cs := range c.consumers {
			c.initconsumer(cs)
		}
	}
	init()
	for _, cs := range c.consumers {
		consumers = append(consumers, cs)
	}
	for {
		select {
		case err := <-c.errors:
			logger.Printf("An error in the connection to the AMQP broker occurred:\n%s", err)
			if c.Reconnect {
				c, _ = NewClient(c.uri, c.Reconnect)
				c.consumers = consumers
				init()
			} else {
				os.Exit(-1)
			}
		case msg := <-c.aggregationChan:
			go func() {
				msg.handler(*msg.delivery)
			}()
		}
	}
}

// Close closes the connection to the AMQP broker.
func (c *Client) Close() {
	c.connection.Close()
}

// AddConsumer adds a consumer to the list of consumers that need to be created
// each time the client is set up. Note that this just adds the consumers to a
// list, it doesn't actually start handling messages yet. You need to call
// Listen() for that.
func (c *Client) AddConsumer(exchange, queue, key string, handler MessageHandler) {
	cs := &consumer{
		exchange: exchange,
		queue:    queue,
		key:      key,
		handler:  handler,
	}
	c.consumers = append(c.consumers, cs)
}

func (c *Client) initconsumer(cs *consumer) error {
	channel, err := c.connection.Channel()
	if err != nil {
		return err
	}
	err = channel.ExchangeDeclare(
		cs.exchange, //name
		"topic",     //kind
		true,        //durable
		false,       //auto-delete
		false,       //internal
		false,       //no-wait
		nil,         //args
	)
	_, err = channel.QueueDeclare(
		cs.queue,
		true,  //durable
		false, //auto-delete
		false, //internal
		false, //no-wait
		nil,   //args
	)
	err = channel.QueueBind(
		cs.queue,
		cs.key,
		cs.exchange,
		false, //no-wait
		nil,   //args
	)

	d, err := channel.Consume(
		cs.queue,
		"",    //consumer tag - auto-assigned in this case
		false, //auto-ack
		false, //exclusive
		false, //no-local
		false, //no-wait
		nil,   //args
	)
	if err != nil {
		return err
	}
	go func() {
		for msg := range d {
			c.aggregationChan <- aggregationMessage{
				handler:  cs.handler,
				delivery: &msg,
			}
		}
	}()
	return err
}
