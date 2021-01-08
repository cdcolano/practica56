package ud.prog3.pr0506d;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

public class GestionTwitter {
	static HashMap<String,UsuarioTwitter> usuarioID;
	static HashMap<String, UsuarioTwitter>usuarioNom;
	
	public static void main(String[] args) {
		try {
			usuarioID= new HashMap<String, UsuarioTwitter>();
			usuarioNom= new HashMap<String, UsuarioTwitter>();
			// TODO Configurar el path y ruta del fichero a cargar
			String fileName = "C:\\Users\\cdcol\\eclipse-workspace\\pr56 alternativa\\data.csv";
			CSV.processCSV( new File( fileName ) );
			rellenaMapa();
			amigos();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void rellenaMapa() {
		for (String clave:usuarioID.keySet()) {
			UsuarioTwitter u=usuarioID.get(clave);
			if (!GestionTwitter.usuarioNom.containsKey(u.getScreenName())) {
				usuarioNom.put(u.getScreenName(), u);
			}else {
				System.err.println("Error Usuario duplicado");
			}
		}
	}
	
	public static void amigos() {
		int tienenDentro=0;
		for (String clave:usuarioNom.keySet()) {
			UsuarioTwitter u=usuarioNom.get(clave);
			int estan=0;
			int noEstan=0;
			for (String ID : u.getFriends()) {
				if (usuarioID.containsKey(ID)) {
					estan++;
				}else {
					noEstan++;
				}
			}
			if (estan>0) {
				tienenDentro++;
			}
			System.out.println("Usuario " + u.getScreenName() + "tiene " + noEstan+ 
					" amigos fuera de nuestro sistema y "+ estan+ " dentro");
		}
		System.out.println("Hay " + tienenDentro + "con algunos amigos dentro de nuestro sistema");
		
	}
	
}
