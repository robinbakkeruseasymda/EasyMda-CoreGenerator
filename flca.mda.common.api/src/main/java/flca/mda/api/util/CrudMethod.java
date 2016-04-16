package flca.mda.api.util;

import mda.annotation.RestService;
import mda.annotation.crud.CrudOperation;

public class CrudMethod {

	private Class<?> entity;
	private CrudOperation crudOper;
	private RestService restServiceAnnot;
	private boolean usePaging;
	private Class<?> searchArguments;


	public CrudMethod(Class<?> entity, CrudOperation crudOper, RestService restServiceAnnot) {
		super();
		this.entity = entity;
		this.crudOper = crudOper;
		this.restServiceAnnot = restServiceAnnot;
	}


	public Class<?> getEntity() {
		return entity;
	}


	public void setEntity(Class<?> entity) {
		this.entity = entity;
	}


	public CrudOperation getCrudOper() {
		return crudOper;
	}


	public void setCrudOper(CrudOperation crudOper) {
		this.crudOper = crudOper;
	}


	public RestService getRestServiceAnnot() {
		return restServiceAnnot;
	}


	public void setRestServiceAnnot(RestService restServiceAnnot) {
		this.restServiceAnnot = restServiceAnnot;
	}

	

	public boolean isUsePaging() {
		return usePaging;
	}


	public void setUsePaging(boolean usePaging) {
		this.usePaging = usePaging;
	}


	public Class<?> getSearchArguments() {
		return searchArguments;
	}


	public void setSearchArguments(Class<?> searchArguments) {
		this.searchArguments = searchArguments;
	}


	@Override
	public String toString()
	{
		String result = (entity != null) ? entity.getSimpleName() : "null";
		result += ", ";
		result += (crudOper != null) ? crudOper.name() : "null";
		
		return result;
	}	
	
	
}
