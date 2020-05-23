import javax.servlet.http.HttpServletRequest;

public class Dummy {
    public void dummy(HttpServletRequest req) {
        if (req.getParameter("hoge") == "hoge") {
        }
    }
}
