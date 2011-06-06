package pl.net.bluesoft.rnd.vries.util.servlet;

import pl.net.bluesoft.rnd.vries.util.cache.MapCacheManager;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A helper servlet for manually clearing all the dashboard caches.
 * <p/>Sample use:
 * <pre>
 *     http://localhost:18080/cacheManager?clear=1
 * </pre>
 */
public class ClearCacheServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String clear = req.getParameter("clear");
        if (clear != null && "1".equalsIgnoreCase(clear)) {
            MapCacheManager.invalidateAllCaches();
            writeResponse(resp, "All dashboard caches cleared...");
        }
        writeResponse(resp, "No caches cleared. Use: <i>" + req.getRequestURL() + "?clear=1</i>");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    private void writeResponse(HttpServletResponse resp, String msg) throws IOException {
        resp.setContentType("text/html");
        ServletOutputStream out = resp.getOutputStream();
        out.println("<html><head><title>Clear Dashboard Caches</title></head><body><h1>");
        out.println(msg);
        out.println("</h1></body></html>");
    }
}
