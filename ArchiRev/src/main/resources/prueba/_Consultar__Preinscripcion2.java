package prueba;

/*@lineinfo:filename=/Consultar_Preinscripcion2.jsp*/
/*@lineinfo:generated-code*/

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import domain.*;
import persistence.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;

public class _Consultar__Preinscripcion2 extends oracle.jsp.runtime.HttpJsp {

	public final String _globalsClassName = null;

	// ** Begin Declarations

	/* @lineinfo:user-code *//* @lineinfo:137^1 */
	protected static int esta(int x, int[] pks) {
		for (int i = 0; i < pks.length; i++)
			if (pks[i] == x)
				return i;
		return -1;
	}

	/* @lineinfo:generated-code */

	// ** End Declarations
	public void _jspService(HttpServletRequest request,
			HttpServletResponse response) throws java.io.IOException,
			ServletException {

		response.setContentType("text/html;charset=windows-1252");
		/*
		 * set up the intrinsic variables using the pageContext goober: session
		 * = HttpSession application = ServletContext out = JspWriter page =
		 * this config = ServletConfig all session/app beans declared in
		 * globals.jsa
		 */
		PageContext pageContext = JspFactory.getDefaultFactory()
				.getPageContext(this, request, response, "errors.jsp", true,
						JspWriter.DEFAULT_BUFFER, true);
		// Note: this is not emitted if the session directive == false
		HttpSession session = pageContext.getSession();
		if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED,
				PageContext.REQUEST_SCOPE) != null) {
			pageContext.setAttribute(OracleJspRuntime.JSP_PAGE_DONTNOTIFY,
					"true", PageContext.PAGE_SCOPE);
			JspFactory.getDefaultFactory().releasePageContext(pageContext);
			return;
		}
		int __jsp_tag_starteval;
		ServletContext application = pageContext.getServletContext();
		JspWriter out = pageContext.getOut();
		_Consultar__Preinscripcion2 page = this;
		ServletConfig config = pageContext.getServletConfig();

		try {
			// global beans
			// end global beans

			out.write(__oracle_jsp_text[0]);
			out.write(__oracle_jsp_text[1]);
			out.write(__oracle_jsp_text[2]);
			out.write(__oracle_jsp_text[3]);
			/* @lineinfo:user-code *//* @lineinfo:6^1 */
			if (session == null) {
				session
						.setAttribute("NombreExcepcion",
								"La sesión ya estaba cerrada o su tiempo había expirado");
				throw new Exception("Sesión no iniciada o tiempo expirado");
			}

			User usu = (User) session.getAttribute("user");
			Broker bd = (Broker) session.getAttribute("bd");
			Broker bd2 = bd.getOtroBroker();
			if (usu == null || bd == null) {
				session
						.setAttribute("NombreExcepcion",
								"La sesión ya estaba cerrada o su tiempo había expirado");
				throw new Exception("Sesión no iniciada o tiempo expirado");
			}

			/* @lineinfo:generated-code */
			out.write(__oracle_jsp_text[4]);
			out.write(__oracle_jsp_text[5]);
			/* @lineinfo:user-code *//* @lineinfo:21^1 */
			String NombreCurso = "";
			int codigo = Integer.parseInt(request.getParameter("id"));
			try {
				String SQL = "";
				SQL = "Select CEP FROM CEP WHERE CODIGO=? AND centro=? ";
				PreparedStatement sentencia = bd.prepareStatement(SQL);
				ResultSet rs = sentencia.executeQuery();
				if (rs.next()) {
					NombreCurso = rs.getString(1);
				}
				rs.close();
				rs = null;

				/* @lineinfo:generated-code */
				out.write(__oracle_jsp_text[6]);
				/* @lineinfo:user-code *//* @lineinfo:45^43 */out
						.print(NombreCurso);
				/* @lineinfo:generated-code */
				out.write(__oracle_jsp_text[7]);
				/* @lineinfo:user-code *//* @lineinfo:48^1 */
				String tabla = "";
				String DNI = "";
				SQL = "Select MATRICULASCEP.DNI,NOMBRE,APELLIDOS,TIPO,ORDEN,ADMITIDO from MATRICULASCEP, ALUMNOSCEP where CEP=? AND MATRICULASCEP.DNI=ALUMNOSCEP.DNI ORDER BY APELLIDOS";
				sentencia = bd2.prepareStatement(SQL);
				ResultSet r = sentencia.executeQuery();
				ResultSetMetaData meta = r.getMetaData();
				int cols = meta.getColumnCount();

				/* @lineinfo:generated-code */
				out.write(__oracle_jsp_text[8]);
				/* @lineinfo:user-code *//* @lineinfo:59^1 */
				int[] colsPK = { 1 };
				for (int i = 1; i <= cols; i++) {
					if (esta(i, colsPK) != -1) {

						/* @lineinfo:generated-code */
						out.write(__oracle_jsp_text[9]);
						/* @lineinfo:user-code *//* @lineinfo:64^11 */out
								.print(meta.getColumnLabel(i));
						/* @lineinfo:generated-code */
						out.write(__oracle_jsp_text[10]);
						/* @lineinfo:user-code *//* @lineinfo:65^1 */} else {
						/* @lineinfo:generated-code */
						out.write(__oracle_jsp_text[11]);
						/* @lineinfo:user-code *//* @lineinfo:66^8 */out
								.print(meta.getColumnLabel(i));
						/* @lineinfo:generated-code */
						out.write(__oracle_jsp_text[12]);
						/* @lineinfo:user-code *//* @lineinfo:67^1 */}
				}
				/* @lineinfo:generated-code */
				out.write(__oracle_jsp_text[13]);
				/* @lineinfo:user-code *//* @lineinfo:70^2 */
				int tipo = 0, admitido = 0;
				while (r.next()) {

					/* @lineinfo:generated-code */
					out.write(__oracle_jsp_text[14]);
					/* @lineinfo:user-code *//* @lineinfo:75^2 */
					String resto = new String();
					for (int i = 1; i <= cols; i++) {

						/* @lineinfo:generated-code */
						out.write(__oracle_jsp_text[15]);
						/* @lineinfo:user-code *//* @lineinfo:80^2 */
						Object valor = r.getObject(i);
						if (meta.getColumnLabel(i).equals("DNI")) {
							DNI = String.valueOf(valor);
						}
						if (meta.getColumnLabel(i).equals("TIPO")) {
							tipo = Integer.parseInt(String.valueOf(valor));
							switch (tipo) {
							case 1:
								valor = "Estudiante Quinto y Tercer Curso";
								break;
							case 2:
								valor = "Estudiante de otro curso";
								break;
							case 3:
								valor = "Alumno de la UCLM";
								break;
							case 4:
								valor = "Personal de la UCLM";
								break;
							case 5:
								valor = "ExAlumnos de la ESI";
								break;
							case 6:
								valor = "Otros";
								break;
							default:
								valor = "Profesional con Experiencia mínima de un año";
							}
						} else if (meta.getColumnLabel(i).equals("ADMITIDO")) {
							admitido = Integer.parseInt(String.valueOf(valor));
							valor = new String();
							if (admitido == 1) {

								/* @lineinfo:generated-code */
								out.write(__oracle_jsp_text[16]);
								/* @lineinfo:user-code *//* @lineinfo:108^49 */out
										.print(DNI);
								/* @lineinfo:generated-code */
								out.write(__oracle_jsp_text[17]);
								/* @lineinfo:user-code *//* @lineinfo:109^2 */} else {
								/* @lineinfo:generated-code */
								out.write(__oracle_jsp_text[18]);
								/* @lineinfo:user-code *//* @lineinfo:110^49 */out
										.print(DNI);
								/* @lineinfo:generated-code */
								out.write(__oracle_jsp_text[19]);
								/* @lineinfo:user-code *//* @lineinfo:111^1 */}
						} else if (valor == null) {
							valor = new String();
						}

						/* @lineinfo:generated-code */
						out.write(__oracle_jsp_text[20]);
						/* @lineinfo:user-code *//* @lineinfo:117^5 */out
								.print(valor);
						/* @lineinfo:generated-code */
						out.write(__oracle_jsp_text[21]);
						/* @lineinfo:user-code *//* @lineinfo:119^1 */}

					/* @lineinfo:generated-code */
					out.write(__oracle_jsp_text[22]);
					/* @lineinfo:user-code *//* @lineinfo:122^1 */}
				r.close();
				r = null;
			} finally {
				bd2.getOtroBroker();
			}

			/* @lineinfo:generated-code */
			out.write(__oracle_jsp_text[23]);
			/* @lineinfo:user-code *//* @lineinfo:133^41 */out.print(codigo);
			/* @lineinfo:generated-code */
			out.write(__oracle_jsp_text[24]);

		} catch (Throwable e) {
			try {
				if (out != null)
					out.clear();
			} catch (Exception clearException) {
			}
			pageContext.handlePageException(e);
		} finally {
			OracleJspRuntime.extraHandlePCFinally(pageContext, false);
			JspFactory.getDefaultFactory().releasePageContext(pageContext);
		}

	}

	private static final char __oracle_jsp_text[][] = new char[25][];
	static {
		try {
			__oracle_jsp_text[0] = "\n".toCharArray();
			__oracle_jsp_text[1] = "\n".toCharArray();
			__oracle_jsp_text[2] = "\n".toCharArray();
			__oracle_jsp_text[3] = "\n\n".toCharArray();
			__oracle_jsp_text[4] = "\n".toCharArray();
			__oracle_jsp_text[5] = "\n".toCharArray();
			__oracle_jsp_text[6] = "\n\n<HTML>\n<HEAD>\n<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=windows-1252\">\n<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"estilo.css\">\n\n<TITLE>\nListado de Preinscripciones\n</TITLE>\n</HEAD>\n<BODY>\n<h2>Listado de Preinscripciones al curso \""
					.toCharArray();
			__oracle_jsp_text[7] = "\"</h2>\n<form name=ppal method=post action=\"Aceptar_Preinscripcion.jsp\">\n<br>\n"
					.toCharArray();
			__oracle_jsp_text[8] = "\n\t<table border=1>\n\t\t<tr>\n"
					.toCharArray();
			__oracle_jsp_text[9] = "\n\t\t\t<td><u>".toCharArray();
			__oracle_jsp_text[10] = "</u></td>\n".toCharArray();
			__oracle_jsp_text[11] = "\n\t\t\t<td>".toCharArray();
			__oracle_jsp_text[12] = "</td>\n".toCharArray();
			__oracle_jsp_text[13] = "\t\t\n\t\t</tr>\n\t".toCharArray();
			__oracle_jsp_text[14] = "\n\t\t<tr>\n\t".toCharArray();
			__oracle_jsp_text[15] = "\n\t\t\t<td>\n\t".toCharArray();
			__oracle_jsp_text[16] = "\t\t\t\t\n\t\t\t\t\t\t\t<center><input type=checkbox name=Aceptar"
					.toCharArray();
			__oracle_jsp_text[17] = " checked></center>\n\t".toCharArray();
			__oracle_jsp_text[18] = "\n\t\t\t\t\t\t\t<center><input type=checkbox name=Aceptar"
					.toCharArray();
			__oracle_jsp_text[19] = "></center>\n".toCharArray();
			__oracle_jsp_text[20] = "\t\t\t\n\t\t\t\t".toCharArray();
			__oracle_jsp_text[21] = "\n\t\t\t</td>\n".toCharArray();
			__oracle_jsp_text[22] = "\n\t\t</tr>\n".toCharArray();
			__oracle_jsp_text[23] = "\n\t</table>\n\t<br><br>\n<input type=submit class=boton name=BOTON value='Aceptar Preinscripciones'>\n<INPUT TYPE=\"hidden\" NAME=idcurso VALUE="
					.toCharArray();
			__oracle_jsp_text[24] = "> \n</form>\n</BODY>\n</HTML>\n"
					.toCharArray();
		} catch (Throwable th) {
			System.err.println(th);
		}
	}
}
