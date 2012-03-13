package com.predic8.membrane.core.interceptor.schemavalidation;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.predic8.membrane.core.Constants;
import com.predic8.membrane.core.exchange.Exchange;
import com.predic8.membrane.core.http.Message;
import com.predic8.membrane.core.http.Response;
import com.predic8.membrane.core.interceptor.Outcome;
import com.predic8.membrane.core.interceptor.schemavalidation.ValidatorInterceptor.FailureHandler;
import com.predic8.membrane.core.util.ResourceResolver;
import com.predic8.schema.Schema;

public abstract class AbstractXMLSchemaValidator implements IValidator {
	private static Log log = LogFactory.getLog(AbstractXMLSchemaValidator.class.getName());

	private final ArrayBlockingQueue<List<Validator>> validators;
	protected final String location;
	protected final ResourceResolver resourceResolver;
	protected final ValidatorInterceptor.FailureHandler failureHandler;
	
	protected final AtomicLong valid = new AtomicLong();
	protected final AtomicLong invalid = new AtomicLong();

	public AbstractXMLSchemaValidator(ResourceResolver resourceResolver, String location, ValidatorInterceptor.FailureHandler failureHandler) throws Exception {
		this.location = location;
		this.resourceResolver = resourceResolver;
		this.failureHandler = failureHandler;
		int concurrency = Runtime.getRuntime().availableProcessors() * 2;
		validators = new ArrayBlockingQueue<List<Validator>>(concurrency);
		for (int i = 0; i < concurrency; i++)
			validators.add(createValidators());
	}
	
	public Outcome validateMessage(Exchange exc, Message msg) throws Exception {
		return validateMessage(exc, msg.getBodyAsStream());
	}

	public Outcome validateMessage(Exchange exc, InputStream body) throws Exception {
		List<Exception> exceptions = new ArrayList<Exception>();
		List<Validator> vals = validators.take();
		try {
			// the message must be valid for one schema embedded into WSDL 
			for (Validator validator: vals) {
				SchemaValidatorErrorHandler handler = (SchemaValidatorErrorHandler)validator.getErrorHandler();
				try {
					validator.validate(getMessageBody(body));
					if (handler.noErrors()) {
						valid.incrementAndGet();
						return Outcome.CONTINUE;
					}
					exceptions.add(handler.getException());
				} finally {
					handler.reset();
				}
			}
		} catch (Exception e) {
			exceptions.add(e);
		} finally {
			validators.put(vals);
		}
		if (failureHandler == FailureHandler.VOID) {
			exc.setProperty("error", getErrorMsg(exceptions));
		} else if (failureHandler != null) {
			failureHandler.handleFailure(getErrorMsg(exceptions), exc);
			exc.setResponse(createErrorResponse("validation error"));
		} else {
			exc.setResponse(createErrorResponse(getErrorMsg(exceptions)));
		}
		invalid.incrementAndGet();
		return Outcome.ABORT;
	}
	
	protected List<Validator> createValidators() throws Exception {
		SchemaFactory sf = SchemaFactory.newInstance(Constants.XSD_NS);
		List<Validator> validators = new ArrayList<Validator>();
		for (Schema schema : getSchemas()) {
			log.info("Creating validator for schema: " + schema);
			StreamSource ss = new StreamSource(new StringReader(schema.getAsString()));
			ss.setSystemId(location);
			sf.setResourceResolver(resourceResolver.toLSResourceResolver());
			Validator validator = sf.newSchema(ss).newValidator();
			validator.setResourceResolver(resourceResolver.toLSResourceResolver());
			validator.setErrorHandler(new SchemaValidatorErrorHandler());
			validators.add(validator);
		}
		return validators;
	}

	private String getErrorMsg(List<Exception> excs) {
		StringBuffer buf = new StringBuffer();
		buf.append("Validation failed: ");
		for (Exception e : excs) {
			buf.append(e);
			buf.append("; ");
		}
		return buf.toString();
	}

	@Override
	public long getValid() {
		return valid.get();
	}

	@Override
	public long getInvalid() {
		return invalid.get();
	}
	
	protected abstract List<Schema> getSchemas();
	protected abstract Source getMessageBody(InputStream input) throws Exception;
	protected abstract Response createErrorResponse(String message);

}
