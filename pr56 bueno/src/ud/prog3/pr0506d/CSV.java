package ud.prog3.pr0506d;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/** Clase para proceso bÃ¡sico de ficheros csv
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
public class CSV { 
	
	private static boolean LOG_CONSOLE_CSV = false;  // Log a consola de lo que se va leyendo en el CSV

	/** Procesa un fichero csv
	 * @param file	Fichero del csv
	 * @throws IOException
	 */
	public static void processCSV( File file ) 
	throws IOException // Error de I/O
	{
		processCSV( file.toURI().toURL() );
	}
	
	/** Procesa un fichero csv
	 * @param urlCompleta	URL del csv
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws FileNotFoundException
	 * @throws ConnectException
	 */
	public static void processCSV( URL url ) 
	throws MalformedURLException,  // URL incorrecta 
	 IOException, // Error al abrir conexiÃ³n
	 UnknownHostException, // servidor web no existente
	 FileNotFoundException, // En algunos servidores, acceso a pï¿½gina inexistente
	 ConnectException // Error de timeout
	{
		BufferedReader input = null;
		InputStream inStream = null;
		try {
		    URLConnection connection = url.openConnection();
		    connection.setDoInput(true);
		    inStream = connection.getInputStream();
		    input = new BufferedReader(new InputStreamReader( inStream, "UTF-8" ));  // Supone utf-8 en la codificaciÃ³n de texto
		    String line = "";
		    int numLine = 0;
		    while ((line = input.readLine()) != null) {
		    	numLine++;
		    	if (LOG_CONSOLE_CSV) System.out.println( numLine + "\t" + line );
		    	try {
			    	ArrayList<Object> l = processCSVLine( input, line, numLine );
			    	if (LOG_CONSOLE_CSV) System.out.println( "\t" + l.size() + "\t" + l );
			    	if (numLine==1) {
			    		procesaCabeceras( l );
			    	} else {
			    		if (!l.isEmpty())
			    			procesaLineaDatos( l );
			    	}
		    	} catch (StringIndexOutOfBoundsException e) {
		    		/* if (LOG_CONSOLE_CSV) */ System.err.println( "\tError: " + e.getMessage() );
		    	}
		    }
		} finally {
			try {
				inStream.close();
				input.close();
			} catch (Exception e2) {
			}
		}
	}
	
		/** Procesa una lÃ­nea de entrada de csv	
		 * @param input	Stream de entrada ya abierto
		 * @param line	La lÃ­nea YA LEÃ�DA desde input
		 * @param numLine	NÃºmero de lÃ­nea ya leÃ­da
		 * @return	Lista de objetos procesados en el csv. Si hay algÃºn string sin acabar en la lÃ­nea actual, lee mÃ¡s lÃ­neas del input hasta que se acaben los strings o el input
		 * @throws StringIndexOutOfBoundsException
		 */
		public static ArrayList<Object> processCSVLine( BufferedReader input, String line, int numLine ) throws StringIndexOutOfBoundsException {
			ArrayList<Object> ret = new ArrayList<>();
			ArrayList<Object> lista = null; // Para posibles listas internas
			int posCar = 0;
			boolean inString = false; // Marca de cuando se estÃ¡ leyendo un string
			boolean lastString = false;  // Marca que el Ãºltimo leÃ­do era un string
			boolean inList = false; // Marca de cuando se estÃ¡ leyendo una lista (entre corchetes, separada por comas)
			boolean finString = false;
			String stringActual = "";
			char separador = 0;
			while (line!=null && (posCar<line.length() || line.isEmpty() && posCar==0)) {
				if (line.isEmpty() && posCar==0) {
					if (!inString) return ret;  // LÃ­nea vacÃ­a
				} else {
					char car = line.charAt( posCar );
					if (car=='"') {
						if (inString) {
							if (nextCar(line,posCar)=='"') {  // Doble "" es un "
								posCar++;
								stringActual += "\"";
							} else {  // " de cierre
								inString = false;
								finString = true;
								lastString = true;
							}
						} else {  // !inString
							if (stringActual.isEmpty()) {  // " de apertura
								inString = true;
							} else {  // " despuÃ©s de valor - error
								throw new StringIndexOutOfBoundsException( "\" after data in char " + posCar + " of line [" + line + "]" );
							}
						}
					} else if (!inString && (car==' ' || car=='\t')) {  // separador fuera de string
						// Nada que hacer
					} else if (car==',' || car==';') {
						if (inString) {  // separador dentro de string
							stringActual += car;
						} else {  // separador que separa valores
							if (separador==0) { // Si no se habÃ­a encontrado separador hasta ahora
								separador = car;
								if (inList)
									lista.add( getDato( stringActual, lastString ) );
								else if (lista!=null) {
									ret.add( lista );
									lista = null;
								} else 
									ret.add( getDato( stringActual, lastString ) );
								stringActual = "";
								lastString = false;
								finString = false;
							} else { // Si se habÃ­a encontrado, solo vale el mismo (, o ;)
								if (separador==car) {  // Es un separador
									if (inList)
										lista.add( getDato( stringActual, lastString ) );
									else if (lista!=null) {
										ret.add( lista );
										lista = null;
									} else 
										ret.add( getDato( stringActual, lastString ) );
									stringActual = "";
									lastString = false;
									finString = false;
								} else {  // Es un carÃ¡cter normal
									if (finString) throw new StringIndexOutOfBoundsException( "Data after string in char " + posCar + " of line [" + line + "]");  // valor despuÃ©s de string - error
									stringActual += car;
								}
							}
						}
					} else if (!inString && car=='[') {  // Inicio de lista
						if (inList) throw new StringIndexOutOfBoundsException( "Nested lists not allowed in this process in line " + numLine + ": [" + line + "]");
						inList = true;
						lista = new ArrayList<>();
					} else if (!inString && car==']') {  // Posible fin de lista
						if (!inList) throw new StringIndexOutOfBoundsException( "Closing list not opened in line " + numLine + ": [" + line + "]");
						if (!stringActual.isEmpty()) lista.add( getDato( stringActual, lastString ) );
						stringActual = "";
						inList = false;
					} else {  // CarÃ¡cter dentro de valor
						if (finString) throw new StringIndexOutOfBoundsException( "Data after string in char " + posCar + " of line [" + line + "]");  // valor despuÃ©s de string - error
						stringActual += car;
					}
					posCar++;
				}
				if (posCar>=line.length() && inString) {  // Se ha acabado la lÃ­nea sin acabarse el string. Eso es porque algÃºn string incluye salto de lÃ­nea. Se sigue con la siguiente lÃ­nea
					line = null;
				    try {
						line = input.readLine();
				    	if (LOG_CONSOLE_CSV) System.out.println( "  " + numLine + " (add)\t" + line );
						posCar = 0;
						stringActual += "\n";
					} catch (IOException e) {}  // Si la lÃ­nea es null es que el fichero se ha acabado ya o hay un error de I/O
				}
			}
			if (inString) throw new StringIndexOutOfBoundsException( "String not closed in line " + numLine + ": [" + line + "]");
			if (lista!=null)
				ret.add( lista );
			else if (!stringActual.isEmpty())
				ret.add( getDato( stringActual, lastString ) );
			return ret;
		}

			// Devuelve el siguiente carÃ¡cter (car 0 si no existe el siguiente carÃ¡cter)
			private static char nextCar( String line, int posCar ) {
				if (posCar+1<line.length()) return line.charAt( posCar + 1 );
				else return Character.MIN_VALUE;
			}
			
			// Devuelve el objeto que corresponde a un dato (por defecto String. Si es entero o doble vÃ¡lido, se devuelve ese tipo)
			private static Object getDato( String valor, boolean esString ) {
				if (esString) return valor;
				try {
					long entero = Long.parseLong( valor );
					return new Long( entero );
				} catch (Exception e) {}
				try {
					double doble = Double.parseDouble( valor );
					return new Double( doble );
				} catch (Exception e) {}
				return valor;
			}

	
	private static void procesaCabeceras( ArrayList<Object> cabs ) {
		// TODO Cambiar este proceso si se quiere hacer algo con las cabeceras
		GestionTwitter.v.taTexto.setText( cabs.toString() );  // Saca la cabecera por consola de error
	}

	private static int numLin = 0;
	private static void procesaLineaDatos( ArrayList<Object> datos ) {
		// TODO Cambiar este proceso si se quiere hacer algo con las cabeceras
		numLin++;
		if (datos.size()==10) {
			UsuarioTwitter u= new UsuarioTwitter();
			u.setId((String)datos.get(0));
			u.setScreenName((String)datos.get(1));
			u.setTags((ArrayList<String>)datos.get(2));
			u.setAvatar((String)datos.get(3));
			u.setFollowersCount((long)datos.get(4));
			u.setFriendsCount((long)datos.get(5));
			u.setLang((String)datos.get(6));
			u.setLastSeen((long)datos.get(7));
			u.setTweetId((String)datos.get(8));
			u.setFriends((ArrayList<String>)datos.get(9));
			if (!GestionTwitter.usuarioID.containsKey(u.getId())) {
				GestionTwitter.usuarioID.put(u.getId(), u);
				System.out.println("Exito " + numLin);
			}else {
				GestionTwitter.v.taTexto.setText("\nError Usuario duplicado");
			}
			//GestionTwitter.usuarioNom.put(u.getScreenName(), u); tambien se puede hacer aqui la carga del 2 mapa
			GestionTwitter.v.pbProgreso.setValue(numLin);
		}else {
			System.out.println(datos.size());
			System.out.println(datos.get(0));
		}
		
	}

}
