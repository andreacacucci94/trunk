package gov.nysenate.openleg.api.servlets;

import gov.nysenate.openleg.util.SessionYear;
import gov.nysenate.services.model.Committee;
import gov.nysenate.services.model.Senator;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
// Richiede commento

/**
 * PJDCC - Summary for class responsabilities.
 *
 * @author 
 * @since 
 * @version 
 */
@SuppressWarnings("serial")
public class AdvancedSearchServlet extends HttpServlet
{
    
/** Comments about this class */
    public void init() throws ServletException
    {
        ObjectMapper mapper = new ObjectMapper();
        int sessionYear = SessionYear.getSessionYear();
        ArrayList<Senator> senators = new ArrayList<>();
        ArrayList<Committee> committees = new ArrayList<>();

        try {
            File committeesBase = new File(URLDecoder.decode(SenatorsServlet.class.getClassLoader().getResource("data/committees/").getPath()));
            File committeesDir = new File(committeesBase, String.valueOf(sessionYear));

            if (!committeesDir.exists()) committeesDir.mkdirs();

            
            for (File committeeFile : FileUtils.listFiles(committeesDir, new String[]{"json"}, false)) {
                committees.add(mapper.readValue(committeeFile, Committee.class));
            }

            Collections.sort(committees, new Comparator<Committee>() {
                public int compare(Committee a, Committee b)
                {
                    return a.getName().compareTo(b.getName());
                }
            });

            File senatorsBase = new File(URLDecoder.decode(SenatorsServlet.class.getClassLoader().getResource("data/senators/").getPath()));
            File senatorsDir = new File(senatorsBase, String.valueOf(sessionYear));
            if (!senatorsDir.exists()) senatorsDir.mkdirs();

            
            for (File senatorFile : FileUtils.listFiles(senatorsDir, new String[]{"json"}, false)) {
                senators.add(mapper.readValue(senatorFile, Senator.class));
            }

            Collections.sort(senators, new Comparator<Senator>() {
                public int compare(Senator a, Senator b)
                {
                    return a.getLastName().compareTo(b.getLastName());
                }
            });
        }
        catch (IOException e) {
            System.out.println("Something was wrong");
        }

    }
/** Comments about this class */
    public void doGet(HttpServletRequest request, HttpServletResponse response, ArrayList<Senator> senators, ArrayList<Commitee> committees) throws ServletException, IOException
    {
        request.setAttribute("senators", senators);
        request.setAttribute("committees", committees);
        request.getSession().getServletContext().getRequestDispatcher("/views/advanced.jsp").forward(request, response);
    }

    /**
     * Proxy to doGet
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost(request, response);
    }
}
