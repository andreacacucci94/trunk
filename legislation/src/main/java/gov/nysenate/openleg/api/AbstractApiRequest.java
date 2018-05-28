package gov.nysenate.openleg.api;

import gov.nysenate.openleg.model.BaseObject;
import gov.nysenate.openleg.util.OpenLegConstants;
import gov.nysenate.openleg.util.TextFormatter;

import java.io.IOException; 
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Richiede commento

/**
 * PJDCC - Summary for class responsabilities.
 *
 * @author 
 * @since 
 * @version 
 */
public abstract class AbstractApiRequest implements OpenLegConstants {
    /**
       * Comments about this class
       */
    public static final String DEFAULT_FORMAT = "html";
    /**
       * Comments about this field
       */
    public static final int DEFAULT_PAGE_NUMBER = 1;
    /**
       * Comments about this field
       */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
       * Comments about this field
       */
    protected HttpServletRequest request;
    /**
       * Comments about this field
       */
    protected HttpServletResponse response;
    /**
       * Comments about this field
       */
    protected int pageNumber;
    /**
       * Comments about this field
       */
    protected int pageSize;
    /**
       * Comments about this field
       */
    protected String format;
     /**
       * Comments about this field
       */
    protected ApiEnum apiEnum;
 /**
       * Comments about this field
       */
    public final HashMap<String, String> CONTENT_TYPE;
/** Comments about this class */
    public AbstractApiRequest(HttpServletRequest request, HttpServletResponse response,
            String pageNumber, String pageSize, String format, ApiEnum apiEnum) {

        this(request,
                response,
                getNumber(pageNumber, DEFAULT_PAGE_NUMBER),
                getNumber(pageSize, DEFAULT_PAGE_SIZE),
                format,
                apiEnum);
    }
/** Comments about this class */
    public AbstractApiRequest(HttpServletRequest request, HttpServletResponse response,
            int pageNumber, int pageSize, String format, ApiEnum apiEnum) {
        this.request = request;
        this.response = response;

        this.pageNumber = pageNumber;
        this.pageSize = pageSize;

        if(request.getRequestURL().toString().toLowerCase().contains("m.nysenate.gov")) {
            this.format = "mobile";
        }
        else {
            this.format = thisOrThat(format, DEFAULT_FORMAT).toLowerCase();
        }

        this.apiEnum = apiEnum;

        this.CONTENT_TYPE = new HashMap<String, String>();
        this.CONTENT_TYPE.put("html", "text/html");
        this.CONTENT_TYPE.put("json", "application/json");
        this.CONTENT_TYPE.put("jsonp", "application/json");
        this.CONTENT_TYPE.put("xml", "application/xml");
        this.CONTENT_TYPE.put("rss", "application/rss+xml");
        this.CONTENT_TYPE.put("csv", "text/csv");
        this.CONTENT_TYPE.put("atom", "application/atom+xml");
    }
/** Comments about this class */
    public void execute() throws ApiRequestException, ServletException, IOException {
        if(!isValidFormat())
            throw new ApiRequestException(
                    TextFormatter.append(
                            "Format ",format," invalid for request ",
                            request.getRequestURI()));
        if(!isValidPaging())
            throw new ApiRequestException(
                    TextFormatter.append(
                            "Page size exceeded ",pageSize,", max is ",MAX_PAGE_SIZE));
        if(!hasParameters())
            throw new ApiRequestException(
                    TextFormatter.append(
                            "Check documentation for required parameters, bad request",
                            request.getRequestURI()));

        fillRequest();

        request.setAttribute("contentType", CONTENT_TYPE.get(format));

        if(format.equals("jsonp")) {
            request.setAttribute("viewPath", getView());
            request.getSession().getServletContext().getRequestDispatcher("/views/jsonp.jsp")
            .forward(request, response);
        }
        else {
            request.getSession().getServletContext().getRequestDispatcher(getView())
            .forward(request, response);
        }


    }
/** Comments about this class */
    protected boolean isValidPaging() {
        if(pageSize > MAX_PAGE_SIZE)
            return false;
        return true;
    }
  
/** Comments about this class */
    protected boolean isValidFormat() {
        for(String validFormat:apiEnum.formats()) {
            if(format.equalsIgnoreCase(validFormat)) {
                return true;
            }
        }
        return false;
    }
/** Comments about this class */
    private String thisOrThat(String str1, String str2) {
        if(str1 == null || str1.matches("\\s*"))
            return str2;
        return str1;
    }

    /*
     * append objects + data to request
     * that are required for the view
     */
    public abstract void fillRequest() throws ApiRequestException;

    /*
     * used to specify where the page
     * is forwarding to
     */
    public abstract String getView();

    /*
     * used to check if the request
     * parameters are valid/not null
     */
    public abstract boolean hasParameters();

    /*
     * helper function used on class instantiation
     * to convert string numbers to ints, or if that
     * fails to a default value
     */
    private static int getNumber(String raw, final int def) {
        int ret;
        try {
            ret = new Integer(raw);
            return ret;
        }
        catch (Exception e) {
            return def;
        }
    }
/** Comments about this class */
    public static <T extends ApiEnum> T getApiEnum(T[] array, String view) {
        for(T t:array) {
            if(t.view().equals(view)) {
                return t;
            }
        }
        return null;
    }

    /*
     * arr[x, y, z, z] -> set[x, y, z]
     */
    public static <T extends ApiEnum> HashSet<String> getUniqueFormats(T[] array) {
        HashSet<String> set = new HashSet<String>();
        for(T t:array) {
            set.addAll(Arrays.asList(t.formats()));
        }
        return set;
    }
/** Comments about this class */
    public interface ApiEnum {
        public String view();
        public String[] formats();
        public Class<? extends BaseObject> clazz();
    }
/** Comments about this class */
    @SuppressWarnings("serial")
    public static class ApiRequestException extends Exception {
        public ApiRequestException() {
            super();
        }
        public ApiRequestException(String message) {
            super(message);
        }
        public ApiRequestException(String message, Exception exception) {
            super(message, exception);
        }
    }
}