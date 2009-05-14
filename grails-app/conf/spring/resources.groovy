// Place your Spring DSL code here
beans = {
    loggingInterceptor(org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor){
        logRequest= true
        logResponse= true
        loggerName= 'SpringwsGrailsPlugin'
    }

    validatingInterceptor(org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor){
        schema= '/WEB-INF/Holiday.xsd'
        validateRequest= true
        validateResponse= true
    }

}