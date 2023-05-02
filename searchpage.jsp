<%@ page import="searchengine.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.net.URL"%>
<%@ page import="java.time.LocalDateTime"%>
<%@ page import="java.util.Collections"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Map.Entry"%>
<%@ page import="java.util.stream.Collectors"%>

<html>
    <head>
        <title>Search Engine</title>
    </head>
<body>
<h1>Good Search Engine</h1>
<form method="post" action="searchpage.jsp">
<label for="query-input">Query: </label>
<input type="text" name="query-input" id="query-input">
<input type="submit" value="Submit"> 
</form>

<%
searchengine.Search mySearch = new searchengine.Search("webapps/searchengine/RM"); 
searchengine.InfoStore info = new searchengine.InfoStore("webapps/searchengine/RM");

if(request.getParameter("query-input")!=null)
{
    String query = request.getParameter("query-input");
	out.println("You have searched "+ query);
    searchengine.structs.SearchResult result = mySearch.EvaluateQuery(query);

    for (int i = 0; i < result.documents.length; i++){
        searchengine.structs.PageStore ps = info.getPageInfo(result.documents[i]);
        %>
        <table>
            <tr>
                <td>Score: <%=result.scores[i]%></td>
                <td><a href="<%=ps.url.toString()%>"><%=ps.title%></a></td>
            </tr>
            <tr>
                <td></td>
                <td><a href="<%=ps.url.toString()%>"><%=ps.url.toString()%></a></td>
            </tr>
            <tr>
                <td></td>
                <td><%=ps.lastModified.toString()%>, <%=ps.size%></td>
            </tr>
            <tr>
                <td></td>
                <td><%=ps.lastModified.toString()%>, <%=ps.size%></td>
            </tr>
            <tr>
                <td></td>
                <td><%
                    Map<Integer, Integer> sortedMap = 
                        ps.keyfreqbody.entrySet().stream()
                        .sorted(Entry.comparingByValue())
                        .collect(Collectors.toMap(Entry<Integer, Integer>::getKey, Entry<Integer, Integer>::getValue,
                                                 (e1, e2) -> e1, HashMap::new));
                    int count = 0
                    for (Map.Entry<Integer, Integer> entry: sortedMap.entrySet()){
                        if (count > 4){ break; }
                        out.print(info.getIDKeyword(entry.getKey()));
                        out.print(entry.getValue());
                        if (count != 4){
                            out.print(", ");
                        }
                        count++;
                    }
                    %></td>
            </tr>
        </table>
    <%
    }
}

%>
</body>
</html>