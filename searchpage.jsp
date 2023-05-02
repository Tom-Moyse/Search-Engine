<%@ page import="SearchEngine.*"%>

<html>
    <head>
        <title>Search Engine</title>
    </head>
<body>
<h1>Good Search Engine</h1>
<form method="post" action="searchpage.jsp">
<label for="query-input">Query: </label>
<input type="text" name="query-input" id="query-input">
</form>

<%
Search mySearch = new Search(); 
InfoStore info = new InfoStore();

if(request.getParameter("query-input")!=null)
{
	out.println("You input "+request.getParameter("query-input"));
}
else
{
	out.println("You input nothing");
}

%>
</body>
</html>