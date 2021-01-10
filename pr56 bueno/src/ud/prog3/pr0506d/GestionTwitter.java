package ud.prog3.pr0506d;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class GestionTwitter {
	static HashMap<String,UsuarioTwitter> usuarioID;
	static HashMap<String, UsuarioTwitter>usuarioNom;
	static VentanaGeneral v;
	static File file;
	
	public static void main(String[] args) {
		try {
			usuarioID= new HashMap<String, UsuarioTwitter>();
			usuarioNom= new HashMap<String, UsuarioTwitter>();
			v= new VentanaGeneral();
			// TODO Configurar el path y ruta del fichero a cargar
			CSV.processCSV(file );
			rellenaMapa();
			Thread hilo= new Thread(new Runnable() {
				
				@Override
				public void run() {
					amigos();
					amigos10();
					v.revalidate();
					
				}
			});
			hilo.start();
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
		int total=0;
		v.pbProgreso.setValue(0);
		String texto=v.taTexto.getText();
		for (String clave:usuarioNom.keySet()) {
			total++;
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
			if (texto.isEmpty()) {
				texto=texto +"Usuario " + u.getScreenName() + "tiene " + noEstan+ 
						" amigos fuera de nuestro sistema y "+ estan+ " dentro";
			}else {
			texto=texto+ "\nUsuario " + u.getScreenName() + "tiene " + noEstan+ 
					" amigos fuera de nuestro sistema y "+ estan+ " dentro";
			}
			/*if (total>500) { //TODO quitar esto despues
				break;
			}*/
			v.pbProgreso.setValue(total);
		}
		texto=(texto+"\nHay " + tienenDentro + "con algunos amigos dentro de nuestro sistema");
		v.taTexto.setText(texto);
		
	}
	
	
	public static void amigos10() {
		int mas10=0;
		int total=0;
		int conteoPrueba=0;//TODO eliminar
		v.pbProgreso.setValue(0);
		for (String clave:usuarioNom.keySet()) {
			total++;
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
			if (estan>=10) {
				mas10++;
				Object[] data={
						u.getId(),u.getScreenName(),u.getFollowersCount(),u.getFriendsCount(),u.lang,u.lastSeen
					};
				v.mTabla.addRow(data);
				if (u.tags.contains("#nationaldogday")) {
					conteoPrueba++;
					
				}
			}
			
			/*if (total>500) { //TODO quitar esto despues
				break;
			}*/
			v.pbProgreso.setValue(total);
		}
		System.err.println(conteoPrueba);
		System.out.println(mas10);
		
	}
	
	
	public void comprueba() {
	
	}
	
	
}

