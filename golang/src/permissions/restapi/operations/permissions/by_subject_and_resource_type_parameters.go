package permissions

// This file was generated by the swagger tool.
// Editing this file might prove futile when you re-run the swagger generate command

import (
	"net/http"

	"github.com/go-swagger/go-swagger/errors"
	"github.com/go-swagger/go-swagger/httpkit"
	"github.com/go-swagger/go-swagger/httpkit/middleware"
	"github.com/go-swagger/go-swagger/httpkit/validate"
	"github.com/go-swagger/go-swagger/swag"

	strfmt "github.com/go-swagger/go-swagger/strfmt"
)

// NewBySubjectAndResourceTypeParams creates a new BySubjectAndResourceTypeParams object
// with the default values initialized.
func NewBySubjectAndResourceTypeParams() BySubjectAndResourceTypeParams {
	var (
		lookupDefault bool = bool(false)
	)
	return BySubjectAndResourceTypeParams{
		Lookup: &lookupDefault,
	}
}

// BySubjectAndResourceTypeParams contains all the bound params for the by subject and resource type operation
// typically these are obtained from a http.Request
//
// swagger:parameters bySubjectAndResourceType
type BySubjectAndResourceTypeParams struct {

	// HTTP Request Object
	HTTPRequest *http.Request

	/*True if a permission lookup should be performed. A permission lookup differs from standard permisison retrieval in two ways. First, only the most permissive permission level available to the subject is returned for any given resource. Second, if the subject happens to be a user then permissions granted to groups that the user belongs to are also included in the results. This parameter is optional and defaults to False.
	  In: query
	  Default: false
	*/
	Lookup *bool
	/*The minimum permission level required to qualify for the result set. All permission levels qualify by default.
	  In: query
	*/
	MinLevel *string
	/*The resource type name.
	  Required: true
	  In: path
	*/
	ResourceType string
	/*The external subject identifier.
	  Required: true
	  In: path
	*/
	SubjectID string
	/*The subject type name.
	  Required: true
	  In: path
	*/
	SubjectType string
}

// BindRequest both binds and validates a request, it assumes that complex things implement a Validatable(strfmt.Registry) error interface
// for simple values it will use straight method calls
func (o *BySubjectAndResourceTypeParams) BindRequest(r *http.Request, route *middleware.MatchedRoute) error {
	var res []error
	o.HTTPRequest = r

	qs := httpkit.Values(r.URL.Query())

	qLookup, qhkLookup, _ := qs.GetOK("lookup")
	if err := o.bindLookup(qLookup, qhkLookup, route.Formats); err != nil {
		res = append(res, err)
	}

	qMinLevel, qhkMinLevel, _ := qs.GetOK("min_level")
	if err := o.bindMinLevel(qMinLevel, qhkMinLevel, route.Formats); err != nil {
		res = append(res, err)
	}

	rResourceType, rhkResourceType, _ := route.Params.GetOK("resource_type")
	if err := o.bindResourceType(rResourceType, rhkResourceType, route.Formats); err != nil {
		res = append(res, err)
	}

	rSubjectID, rhkSubjectID, _ := route.Params.GetOK("subject_id")
	if err := o.bindSubjectID(rSubjectID, rhkSubjectID, route.Formats); err != nil {
		res = append(res, err)
	}

	rSubjectType, rhkSubjectType, _ := route.Params.GetOK("subject_type")
	if err := o.bindSubjectType(rSubjectType, rhkSubjectType, route.Formats); err != nil {
		res = append(res, err)
	}

	if len(res) > 0 {
		return errors.CompositeValidationError(res...)
	}
	return nil
}

func (o *BySubjectAndResourceTypeParams) bindLookup(rawData []string, hasKey bool, formats strfmt.Registry) error {
	var raw string
	if len(rawData) > 0 {
		raw = rawData[len(rawData)-1]
	}
	if raw == "" { // empty values pass all other validations
		var lookupDefault bool = bool(false)
		o.Lookup = &lookupDefault
		return nil
	}

	value, err := swag.ConvertBool(raw)
	if err != nil {
		return errors.InvalidType("lookup", "query", "bool", raw)
	}
	o.Lookup = &value

	return nil
}

func (o *BySubjectAndResourceTypeParams) bindMinLevel(rawData []string, hasKey bool, formats strfmt.Registry) error {
	var raw string
	if len(rawData) > 0 {
		raw = rawData[len(rawData)-1]
	}
	if raw == "" { // empty values pass all other validations
		return nil
	}

	o.MinLevel = &raw

	if err := o.validateMinLevel(formats); err != nil {
		return err
	}

	return nil
}

func (o *BySubjectAndResourceTypeParams) validateMinLevel(formats strfmt.Registry) error {

	if err := validate.Enum("min_level", "query", *o.MinLevel, []interface{}{"read", "admin", "write", "own"}); err != nil {
		return err
	}

	return nil
}

func (o *BySubjectAndResourceTypeParams) bindResourceType(rawData []string, hasKey bool, formats strfmt.Registry) error {
	var raw string
	if len(rawData) > 0 {
		raw = rawData[len(rawData)-1]
	}

	o.ResourceType = raw

	return nil
}

func (o *BySubjectAndResourceTypeParams) bindSubjectID(rawData []string, hasKey bool, formats strfmt.Registry) error {
	var raw string
	if len(rawData) > 0 {
		raw = rawData[len(rawData)-1]
	}

	o.SubjectID = raw

	return nil
}

func (o *BySubjectAndResourceTypeParams) bindSubjectType(rawData []string, hasKey bool, formats strfmt.Registry) error {
	var raw string
	if len(rawData) > 0 {
		raw = rawData[len(rawData)-1]
	}

	o.SubjectType = raw

	if err := o.validateSubjectType(formats); err != nil {
		return err
	}

	return nil
}

func (o *BySubjectAndResourceTypeParams) validateSubjectType(formats strfmt.Registry) error {

	if err := validate.Enum("subject_type", "path", o.SubjectType, []interface{}{"user", "group"}); err != nil {
		return err
	}

	return nil
}
