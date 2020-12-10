
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;



public class MainFrame extends JFrame {

	private final int HEIGHT = 600;
	private final int WIDTH = 600;
	private boolean fileLoaded = false;
	private GraphicsDisplay display = new GraphicsDisplay();
	private JCheckBoxMenuItem showAxisMenuItem;
	private JCheckBoxMenuItem showMarkersMenuItem;
	private JMenuItem resetGraphicsMenuItem;
	private JMenuItem shapeRotateClockItem;
	private JMenuItem shapeRotateAntiClockItem;
	private JMenuItem saveToTextMenuItem;
	private JFileChooser fileChooser = null;
	public MainFrame()  {
		super("Вывод графика функции");
		setSize(WIDTH, HEIGHT);
		Toolkit kit = Toolkit.getDefaultToolkit();
		setLocation((kit.getScreenSize().width - WIDTH)/2, (kit.getScreenSize().height - HEIGHT)/2);
		//setExtendedState(MAXIMIZED_BOTH);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu fileMenu = new JMenu("Файл");
		menuBar.add(fileMenu);
		Action openGraphicsAction = new AbstractAction("Открыть файл", 
				new ImageIcon(MainFrame.class.getResource("image/Open_files.png"))) {
			
			public void actionPerformed(ActionEvent arg0) {
				if (fileChooser==null) {
					fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File(".")); 
					} 
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
					openGraphics(fileChooser.getSelectedFile());
				
			}
		};
		fileMenu.add(openGraphicsAction);
		
		Action saveToTextAction = new AbstractAction("Сохранить в .txt", 
				new ImageIcon(MainFrame.class.getResource("image/Save.png"))) {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (fileChooser == null){
					fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File("."));
				}
				if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION){
					display.saveToTextFile(fileChooser.getSelectedFile());
				}
			}
		};
		saveToTextMenuItem = fileMenu.add(saveToTextAction);
		
		JMenu graphicsMenu = new JMenu("График");
		menuBar.add(graphicsMenu);
		
		/*Action rotatesShapeClockAction = new AbstractAction("Повернуть по часовой стрелке") {
			
			public void actionPerformed(ActionEvent e) {	
				if(display.isClockRotate()){
					display.setClockRotate(false);
					display.setAntiClockRotate(false);
				}else
					display.setClockRotate(true);
			}
		};
		shapeRotateClockItem = new JCheckBoxMenuItem(rotatesShapeClockAction); 
		graphicsMenu.add(shapeRotateClockItem);
		shapeRotateClockItem.setEnabled(false);*/
		
		Action rotatesShapeAntiClockAction = new AbstractAction("Повернуть ВЛЕВО") {
			public void actionPerformed(ActionEvent e) {
				if(display.isAntiClockRotate())
				{
					display.setClockRotate(false);
					display.setAntiClockRotate(false);
				}
				else					
					display.setAntiClockRotate(true);
			}
		};
		shapeRotateAntiClockItem = new JCheckBoxMenuItem(rotatesShapeAntiClockAction); 
		graphicsMenu.add(shapeRotateAntiClockItem);
		shapeRotateAntiClockItem.setEnabled(false);
		graphicsMenu.addSeparator();
		
		Action showAxisAction = new AbstractAction("Показать оси координат") {
			public void actionPerformed(ActionEvent e) {
				display.setShowAxis(showAxisMenuItem.isSelected());
			}
		};
		showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
		graphicsMenu.add(showAxisMenuItem);
		showAxisMenuItem.setSelected(true);
		    
		    
		Action showMarkersAction = new AbstractAction("Показать маркеры точек") {
			
			public void actionPerformed(ActionEvent e) {
			display.setShowMarkers(showMarkersMenuItem.isSelected());	
			}
		};
		showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
		graphicsMenu.add(showMarkersMenuItem);
		showMarkersMenuItem.setSelected(true);
		graphicsMenu.addSeparator();
		
		Action resetGraphicsAction = new AbstractAction("Отменить все изменения") {
		      public void actionPerformed(ActionEvent event) {
		        MainFrame.this.display.reset();
		      }
		    };
		    resetGraphicsMenuItem = new JMenuItem(resetGraphicsAction);
		    graphicsMenu.add(resetGraphicsMenuItem);
		    resetGraphicsMenuItem.setEnabled(false);
		graphicsMenu.addMenuListener(new GraphicsMenuListener());
		getContentPane().add(display, BorderLayout.CENTER);
	}
	ArrayList graphicsData = new ArrayList(50);
	protected void openGraphics(File selectedFile) {
			 try {
			      DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
			      int i=0;
			      while (in.available() > 0) {

			        double x = in.readDouble();
			        double y = in.readDouble();
			        graphicsData.add(new Double[] { x, y });
			        System.out.println(graphicsData.size());
			        i++;
			      }
			      if (graphicsData.size() > 0) {
			        fileLoaded = true;
			        resetGraphicsMenuItem.setEnabled(true);
			        display.showGraphics(graphicsData);
			      }
		}catch (FileNotFoundException e){
			
		}catch (IOException e){
			
			}
		}

	private class GraphicsMenuListener implements MenuListener {

		@Override
		public void menuCanceled(MenuEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void menuDeselected(MenuEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void menuSelected(MenuEvent arg0) {
			
			// TODO Auto-generated method stub
			showAxisMenuItem.setEnabled(fileLoaded);
			showMarkersMenuItem.setEnabled(fileLoaded);
			shapeRotateAntiClockItem.setEnabled(fileLoaded);
			//shapeRotateClockItem.setEnabled(fileLoaded);
			saveToTextMenuItem.setEnabled(fileLoaded);
		}
	}
	public static void main(String[] args) {
		MainFrame frame = new MainFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}
}
