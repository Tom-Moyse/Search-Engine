<%@ page import="searchengine.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.net.URL"%>
<%@ page import="java.time.LocalDateTime"%>
<%@ page import="java.util.Collections"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.LinkedHashMap"%>
<%@ page import="java.util.Map.Entry"%>
<%@ page import="java.util.stream.Collectors"%>

<html>
    <head>
        <title>Search Engine</title>
        <link rel="stylesheet" href="styles.css">
    </head>
<body>
    <div class="center" id="header-div">
<h1>Good Search Engine</h1>
<form method="post" action="searchpage.jsp">
<label for="query-input">Query: </label>
<input type="text" name="query-input" id="query-input">
<input type="submit" value="Submit"> 
</form>

<%
searchengine.Search mySearch = new searchengine.Search("webapps/searchengine"); 
searchengine.InfoStore info = new searchengine.InfoStore("webapps/searchengine/RM");

if(request.getParameter("query-input")!=null)
{
    String query = request.getParameter("query-input");
	out.println("<h3>You have searched for '"+ query + "'</h3></div>");
    searchengine.structs.SearchResult result = mySearch.EvaluateQuery(query);

    for (int i = 0; i < result.documents.length; i++){
        searchengine.structs.PageStore ps = info.getPageInfo(result.documents[i]);
        %>
        <table class="center">
            <caption>Score: <%=result.scores[i]%></caption>
            <tr>
                <td>Title</td>
                <td><a href="<%=ps.url.toString()%>"><%=ps.title%></a></td>
            </tr>
            <tr>
                <td>URL</td>
                <td><a href="<%=ps.url.toString()%>"><%=ps.url.toString()%></a></td>
            </tr>
            <tr>
                <td>Meta Info</td>
                <td>Last Modified: <%=ps.lastModified.toLocalDate().toString()%> <%=ps.lastModified.toLocalTime().toString()%>;<br>Size: <%=ps.size%></td>
            </tr>
            <tr>
                <td>Top Keywords</td>
                <td><%
                    Map<Integer, Integer> sortedMap = 
                        ps.keyfreqbody.entrySet().stream()
                        .sorted(Entry.<Integer, Integer>comparingByValue().reversed())
                        .collect(Collectors.toMap(Entry<Integer, Integer>::getKey, Entry<Integer, Integer>::getValue,
                                                 (e1, e2) -> e1, LinkedHashMap::new));
                    int count = 0;
                    for (Map.Entry<Integer, Integer> entry: sortedMap.entrySet()){
                        if (count > 4){ break; }
                        out.print("Keyword: ");
                        out.print(info.getIDKeyword(entry.getKey()));
                        out.print(", Frequency: ");
                        out.print(entry.getValue());
                        if (count != 4){
                            out.print(";<br>");
                        }
                        count++;
                    }
                    %></td>
            </tr>
            <%
            count = 0;
            for (Integer pid: ps.parentIDs){
                count++;
                if (count > 5){
                    %>
                    <tr>
                        <td>Additional Parent Links</td>
                        <td><%=ps.parentIDs.size() - count + 1%></td>
                    </tr>
                    <%
                    break;
                }
            %>
            <tr>
                <td>Parent Link <%=count%></td>
                <td><%=info.getPageInfo(pid).url.toString()%></td>
            </tr>
            <%
            }
            count = 0;
            for (Integer cid: ps.childIDs){
                count++;
                if (count > 5){
                    %>
                    <tr>
                        <td>Additional Child Links</td>
                        <td><%=ps.childIDs.size() - count + 1%></td>
                    </tr>
                    <%
                    break;
                }
            %>
            <tr>
                <td>Child Link <%=count%></td>
                <td><%=info.getPageInfo(cid).url.toString()%></td>
            </tr>
            <%
            }
            %>
            
        </table>
    <%
    }
}

%>
</body>
</html>