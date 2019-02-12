/**
 * Copyright (c) 2002-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package rest.batch;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import rest.RequestResult;
import rest.RestRequest;
import rest.batch.RestOperations.RestOperation;
import rest.batch.RestOperations.RestOperation.Methods;
import rest.util.JsonHelper;



public class RecordingRestRequest implements RestRequest {    
  
    private final String baseUri;   
    private MediaType contentType;
    private MediaType acceptHeader;   
    private RestOperations operations;
    private boolean stop;


    public RestOperations getOperations() {
        return operations;
    }

    public RecordingRestRequest(final String baseUri) {
        this(baseUri, MediaType.APPLICATION_JSON_TYPE,  MediaType.APPLICATION_JSON_TYPE );
    }
    
    public RecordingRestRequest(RestOperations operations, final String baseUri) {
       this(baseUri);
       this.operations = operations;
    }   
    
    
    public RecordingRestRequest(String baseUri, MediaType contentType, MediaType acceptHeader) {
        this.baseUri = uriWithoutSlash( baseUri );
        this.contentType = contentType;
        this.acceptHeader = acceptHeader;        
    }

    @Override
    public RequestResult get(String path, Object data) {
       return this.record(Methods.GET, path, data, getBaseUri());
    }

    @Override
    public RequestResult delete(String path) {
        return this.record(Methods.DELETE, path, null, getBaseUri());
    }

    @Override
    public RequestResult post(String path, Object data) {       
        return this.record(Methods.POST, path, data, getBaseUri());
    }

    @Override
    public RequestResult put(String path, Object data) {
        return this.record(Methods.PUT, path, data, getBaseUri());
        
    }

    @Override
    public RestRequest with(String uri) {        
        return new RecordingRestRequest(this.operations, uri);
    }

    @Override
    public String getUri() {
        return getBaseUri();
    }

    @Override
    public RequestResult get(String path) {
        return this.record(Methods.GET, path, null, getBaseUri());
    }    
    
    public RequestResult record(Methods method, String path, Object data, String baseUri){
        if (stop) throw new IllegalStateException("BatchRequest already executed");
        return this.operations.record(method, path, data,baseUri);
    }       

    private String uriWithoutSlash( String uri ) {
        String uriString = uri;
        return uriString.endsWith( "/" ) ?  uriString.substring( 0, uriString.length() - 1 )  : uri;
    }

    public static String encode( Object value ) {
        if ( value == null ) return "";
        try {
            return URLEncoder.encode( value.toString(), "utf-8" ).replaceAll( "\\+", "%20" );
        } catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException( e );
        }
    }    
    
    public Map<Long,RestOperation> getRecordedRequests(){
        return this.operations.getRecordedRequests();
    }

    public void stop() {
        this.stop = true;
    }

    public String getBaseUri() {
        return baseUri;
    }

    @Override
	public Map<?, ?> toMap(RequestResult requestResult) {
       return requestResult.toMap();
	}
}

