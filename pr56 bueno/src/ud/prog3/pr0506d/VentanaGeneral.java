package ud.prog3.pr0506d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.event.WindowEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;import java.util.EventObject;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class VentanaGeneral extends JFrame{
	static Properties guardarFich = new Properties();
	private static final String NOM_FICH_PROP="csv.ini";
	TextArea taTexto;
	JFileChooser fcArchivos;
	JProgressBar pbProgreso;
	JTable tabla;
	DefaultTableModel mTabla;
	JTextField tfEtiqueta;
	String texto;
	public VentanaGeneral() {
		
		JPanel pCompleto= new JPanel();
		
		taTexto= new TextArea();
		cargaDatos();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV", "csv");
		fcArchivos.setFileFilter(filter);
		JPanel pCentral= new JPanel();
		pCentral.add(fcArchivos);
		int seleccion=fcArchivos.showOpenDialog(pCentral);
		if (seleccion==JFileChooser.APPROVE_OPTION) {
			GestionTwitter.file=fcArchivos.getSelectedFile();
		}else {
			String ruta=(String)guardarFich.getProperty("archivo");
			File f= new File(ruta);
			GestionTwitter.file=f;
		}
		JPanel pInferior= new JPanel();
		pbProgreso= new JProgressBar(0,40000);
		pInferior.add(pbProgreso);
		
		String[] headers= {
				"id", "screenName", "followersCount", "friendsCount", "lang", "lastSeen"
		};
		
		tabla= new JTable();
		mTabla= new DefaultTableModel();
		tabla.setModel(mTabla);
		mTabla.setColumnIdentifiers(headers);
		
		DefaultMutableTreeNode raiz= new DefaultMutableTreeNode("Usuarios");
		JTree tAmigos= new JTree(raiz);
		
		
		
		JPanel pSuperior=new JPanel();
		
		JScrollPane spSuperior= new JScrollPane(tabla);
		pSuperior.setLayout(new BorderLayout());
		pSuperior.add(spSuperior,BorderLayout.CENTER);
		

		ListSelectionModel selectionModel = tabla.getSelectionModel();

		selectionModel.addListSelectionListener(new ListSelectionListener() {
	

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() && tabla.getSelectedRow()!=-1) {
					DefaultMutableTreeNode raiz=new DefaultMutableTreeNode(mTabla.getValueAt(tabla.getSelectedRow(), 1));
					UsuarioTwitter u= GestionTwitter.usuarioNom.get(mTabla.getValueAt(tabla.getSelectedRow(), 1));
					for (String amigo:u.getFriends()) {
						System.out.println(amigo);
						if (GestionTwitter.usuarioID.containsKey(amigo)) {
							UsuarioTwitter u2=GestionTwitter.usuarioID.get(amigo);
							raiz.add(new DefaultMutableTreeNode(u2.getScreenName()));
						}
					}
					TreeModel mAmigos= new DefaultTreeModel(raiz);
					tAmigos.setModel(mAmigos);
					tAmigos.revalidate();
			
				}
			}
		});
		
		
		JPanel pSupIb= new JPanel();
		JScrollPane pSupIa= new JScrollPane(tAmigos);

		
		JPanel pSupI = new JPanel();
		pSupI.setLayout(new BorderLayout());
		pSupIb.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		tfEtiqueta= new JTextField(40);
		JLabel lEtiqueta = new JLabel("Introduce una etiqueta");
		
		
		
		pSupIb.add(lEtiqueta);
		pSupIb.add(tfEtiqueta);
		
		pSupI.add(pSupIb,BorderLayout.SOUTH);
		pSupI.add(pSupIa,BorderLayout.CENTER);
		pSuperior.add(pSupI,BorderLayout.SOUTH);
		
		
		getContentPane().add(pSuperior,BorderLayout.NORTH);
		getContentPane().add(pInferior,BorderLayout.SOUTH);
		getContentPane().add(pCentral,BorderLayout.CENTER);
		pCentral.removeAll();
		pCentral.add(taTexto);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH); 
		setVisible(true);
		pack();
		
		addWindowListener(new WindowAdapter() {
			
			
			@Override
			public void windowClosed(WindowEvent e) {
				guardarDatos();
			}
			
		
		
		});
		
		tfEtiqueta.addKeyListener(new KeyAdapter() {
			
			
		
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER) {
					texto=VentanaGeneral.this.tfEtiqueta.getText();
					System.out.println("SI");
					for (int i=0;i<tabla.getColumnCount();i++) {
					tabla.getColumnModel().getColumn(i).setCellRenderer(new TablaCellRenderer());
					VentanaGeneral.this.tabla.revalidate();
					}
				}
			}
			
			
		});
		
		TreeSelectionModel selectionModelAmi = tAmigos.getSelectionModel();
		selectionModelAmi.addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
					TreePath path=e.getPath();
					Object [] nodos = path.getPath();
					DefaultMutableTreeNode ultimoNodo=(DefaultMutableTreeNode)nodos[nodos.length-1];
					UsuarioTwitter u= GestionTwitter.usuarioNom.get(ultimoNodo.getUserObject());
					for (String amigo:u.getFriends()) {
						System.out.println(amigo);
						if (GestionTwitter.usuarioID.containsKey(amigo)) {
							UsuarioTwitter u2=GestionTwitter.usuarioID.get(amigo);
							ultimoNodo.add(new DefaultMutableTreeNode(u2.getScreenName()));
						}
					}
				
					tAmigos.revalidate();
			
				}
			
				
			
		});
		
	}
	
	public static void main (String[] args) {
		try {
			GestionTwitter.usuarioID= new HashMap<String, UsuarioTwitter>();
			GestionTwitter.usuarioNom= new HashMap<String, UsuarioTwitter>();
			GestionTwitter.v= new VentanaGeneral();
			// TODO Configurar el path y ruta del fichero a cargar
			CSV.processCSV(GestionTwitter.file );
			GestionTwitter.rellenaMapa();
			Thread hilo= new Thread(new Runnable() {
				
				@Override
				public void run() {
					GestionTwitter.amigos();
					GestionTwitter.amigos10();
					GestionTwitter.v.revalidate();
					
				}
			});
			hilo.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void guardarDatos() {
		guardarFich.put("archivo",GestionTwitter.file.getAbsolutePath());
		
		try {
			ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(NOM_FICH_PROP));
			guardarFich.save(oos,"");
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void cargaDatos() {
		File fichero= new File(NOM_FICH_PROP);
		if (fichero.exists()) {
			try {
				ObjectInputStream ois= new ObjectInputStream(new FileInputStream(NOM_FICH_PROP));
				guardarFich.load(ois);
				File archivo=new File(((String)guardarFich.get("archivo")));
				fcArchivos= new JFileChooser(archivo);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				fcArchivos= new JFileChooser();
			} catch (IOException e) {
				e.printStackTrace();
				fcArchivos= new JFileChooser();
			}
		}else {
			fcArchivos= new JFileChooser();
		}
	}
}

class TablaCellRenderer extends DefaultTableCellRenderer  implements TableCellRenderer{
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		UsuarioTwitter u=GestionTwitter.usuarioNom.get(GestionTwitter.v.mTabla.getValueAt(row, 1));
		JLabel lRet=(JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (u.getTags().contains(GestionTwitter.v.texto)) {
			System.out.println(u.getScreenName());
			lRet.setBackground(Color.GREEN);;
		}else {
			lRet.setBackground(Color.white);
		}
		return lRet;
	}
	
}
