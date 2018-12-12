<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form method="post" action="createPDF">
            <div style="padding:20px; border:solid 2px gray;">
                
                User Id <input type="number" name="p_increment" value="3" >
                
                <br>
                Show HTML <input type="radio" name="p_display" value="HTML" checked="checked" />
                Show PDF <input type="radio" name="p_display" value="PDF" checked="checked"/>
                <br>
                <div style="text-align: center; padding-top:20px;">
                    <input type="submit" value="Display Results using Info Above">
                   
                </div>
                <div style="text-align: center; padding-top: 20px;">
                    <a href="./createPDF?p_increment=3&p_display=PDF">Go Directly to Page</a>
                </div>
            </div>
        </form>
    </body>
</html>
