<jsp:useBean id="managelogspropertiesLogProperties" scope="session" class="fr.paris.lutece.plugins.managelogs.web.LogPropertiesJspBean" />
<% String strContent = managelogspropertiesLogProperties.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
