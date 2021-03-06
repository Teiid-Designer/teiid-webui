<!DOCTYPE html>
<!--[if IE 8]><html class="ie8 login-pf"><![endif]-->
<!--[if IE 9]><html class="ie9 login-pf"><![endif]-->
<!--[if gt IE 9]><!-->
<html class="login-pf">
<!--<![endif]-->
<head>
 <title>Data Virtualization WebUI</title>
 <meta name="viewport" content="width=device-width, initial-scale=1.0">
 <link rel="apple-touch-icon-precomposed" sizes="144x144" href="org.teiid.TeiidWebUI/patternfly/dist/img/apple-touch-icon-144-precomposed.png">
 <link rel="apple-touch-icon-precomposed" sizes="114x114" href="org.teiid.TeiidWebUI/patternfly/dist/img/apple-touch-icon-114-precomposed.png">
 <link rel="apple-touch-icon-precomposed" sizes="72x72" href="org.teiid.TeiidWebUI/patternfly/dist/img/apple-touch-icon-72-precomposed.png">
 <link rel="apple-touch-icon-precomposed" href="org.teiid.TeiidWebUI/patternfly/dist/img/apple-touch-icon-57-precomposed.png">
 <link href="org.teiid.TeiidWebUI/patternfly/dist/css/patternfly.css" rel="stylesheet" media="screen, print">
 <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
 <!--[if lt IE 9]>
 <script src="org.teiid.TeiidWebUI/patternfly/components/html5shiv/dist/html5shiv.min.js"></script>
 <script src="org.teiid.TeiidWebUI/patternfly/components/respond/dest/respond.min.js"></script>
 <![endif]-->
 <!-- IE8 requires jQuery and Bootstrap JS to load in head to prevent rendering bugs -->
 <!-- IE8 requires jQuery v1.x -->
 <script src="org.teiid.TeiidWebUI/patternfly/components/jquery/jquery.min.js"></script>
 <script src="org.teiid.TeiidWebUI/patternfly/components/bootstrap/dist/js/bootstrap.min.js"></script>
 <script src="org.teiid.TeiidWebUI/patternfly/dist/js/patternfly.min.js"></script>
</head>

<body style="background:url('org.teiid.TeiidWebUI/images/login-screen-background.jpg') repeat;">
 <span id="badge">
   <img src="<%=request.getContextPath()%>/org.teiid.TeiidWebUI/images/login-screen-logo.png" alt="Logo" title="Powered By JBoss"/>
 </span>
 <div class="container">
   <div class="row">
     <div class="col-sm-12">
       <div id="brand">
         <img src="<%=request.getContextPath()%>/org.teiid.TeiidWebUI/images/RH-Product-Name.png" alt="" width="400" height="30"  />
       </div><!--/#brand-->
     </div><!--/.col-*-->
     <div class="col-sm-7 col-md-6 col-lg-5 login">
       <form class="form-horizontal" role="form" action="uf_security_check" method="post">

         <% if (request.getParameter("login_failed") != null) { %>
         <div class="alert alert-danger" role="alert">
           <strong>Login Failed.</strong> Please try again.
         </div>
         <% } %>
         
         <div class="form-group">
           <label for="inputUsername" class="col-sm-2 col-md-2 control-label">Username</label>
           <div class="col-sm-10 col-md-10">
             <input name="uf_username" type="text" class="form-control" id="inputUsername" placeholder="" tabindex="1">
           </div>
         </div>
         <div class="form-group">
           <label for="inputPassword" class="col-sm-2 col-md-2 control-label">Password</label>
           <div class="col-sm-10 col-md-10">
             <input name="uf_password" type="password" class="form-control" id="inputPassword" placeholder="" tabindex="2">
           </div>
         </div>
         
         <% if (request.getParameter("gwt.codesvr") != null) { %>
           <input type="hidden" name="gwt.codesvr" value="<%= org.owasp.encoder.Encode.forHtmlAttribute(request.getParameter("gwt.codesvr")) %>"/>
         <% } %>
         
         <div class="form-group">
           <div class="col-xs-8 col-sm-offset-2 col-sm-6 col-md-offset-2 col-md-6">
         <%--
             <div class="checkbox">
               <label>
                 <input type="checkbox" tabindex="3"> Remember Username
               </label>
             </div>
             <span class="help-block"> Forgot <a href="#" tabindex="5">Username</a> or <a href="#" tabindex="6">Password</a>?</span>
          --%>
           </div>
           <div class="col-xs-4 col-sm-4 col-md-4 submit">
             <button type="submit" class="btn btn-primary btn-lg" tabindex="4">Log In</button>
           </div>
         </div>
       </form>
     </div><!--/.col-*-->
     <div class="col-sm-5 col-md-6 col-lg-7 details">
       <p><strong>Welcome to Data Virtualization WebUi</strong><br>
       <a href="http://www.redhat.com/en/technologies/jboss-middleware/data-virtualization">Red Hat JBoss Data Virtualization</a> turns your fragmented data into actionable information!<br>
     </div><!--/.col-*-->
   </div><!--/.row-->
 </div><!--/.container-->
</body>
</html>
