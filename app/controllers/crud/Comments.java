package controllers.crud;

import play.mvc.With;
import controllers.CRUD;
import controllers.Secure;

@With(Secure.class)
public class Comments extends CRUD {

}
