import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/*********************** 문제점 ***********************/

// 텍스트 저장할때 이미지 저장한 위치 위에는 글자가 깨짐

/*********************** 문제점 ***********************/

public class FileManager extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	Vector<FileInfo> vFile;
	Vector<MediaButton> vFileBtn;
	FileInfo targetFile;
	
	JPanel pan[];

//	int viewWid = 3;
//	int viewHei = 4;
	int viewWid = 2;
	int viewHei = 2;
	int viewCnt = 0;
	int page = 1;
	
	String chooserRoot = "G:\\Users\\fkwls\\Desktop\\하스 스샷";
	String downloadRoot = "G:\\Users\\fkwls\\Desktop\\FileManagerDownload\\";

	public static void main(String[] args) {
		FileManager frame = new FileManager();
		frame.setVisible(true);
	}

	/**
	 * Create the frame.
	 */
	public FileManager() {
		setTitle("File Manager Program");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		makeGUI();

		setResizable(false);
		setBounds(100, 100, 590, 518);
	}

	void makeGUI() {
		vFile = new Vector<FileInfo>();
		vFileBtn = new Vector<MediaButton>();
		contentPane = new JPanel();
		contentPane.setBackground(new Color(57, 174, 169));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		/* 파일 포맷 버튼 */
		String formatStr[] = {"전체", "이미지", "문서", "오디오", "기타"};
		JButton formatBtn[] = new JButton[formatStr.length];
		for(int i = 0 ; i < formatBtn.length ; i++) {
			formatBtn[i] = new JButton(formatStr[i]);
			formatBtn[i].setSize(150, 47);
			formatBtn[i].setLocation(12, 72 + 57*i);
			contentPane.add(formatBtn[i]);
		}
		formatBtn[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("vFile.size(): " + vFile.size());
				System.out.println("vFileBtn.size(): " + vFileBtn.size());
				System.out.println("viewCnt: " + viewCnt);
				System.out.println("page: " + page);
			}
		});
		formatBtn[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				page = Integer.parseInt(textField.getText());
				if(page <= vFile.size()/(viewWid*viewHei)) {
					viewCnt = viewWid*viewHei;
				}
				else {
					viewCnt = vFile.size()%(viewWid*viewHei);
				}
				refresh(pan[0]);
			}
		});
		

		/* 파일정보창 */
		JPanel info_panel = new JPanel();
		info_panel.setBounds(12, 357, 150, 119);
		contentPane.add(info_panel);

		/* 파일 컨트롤 버튼 */
		String controlStr[] = {"추가", "삭제", "다운로드"};
		JButton controlBtn[] = new JButton[controlStr.length];
		for(int i = 0 ; i < controlBtn.length ; i++) {
			controlBtn[i] = new JButton(controlStr[i]);
			controlBtn[i].setSize(110, 47);
			controlBtn[i].setLocation(202 + 122*i, 428);
			contentPane.add(controlBtn[i]);
		}

		/* 탑펜 */
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(174, 64, 400, 354);
		contentPane.add(tabbedPane);


		String panStr[] = {"LIST", "ICON"};
		pan = new JPanel[panStr.length];
		for(int i = 0 ; i < pan.length ; i++) {
			pan[i] = new JPanel();
			tabbedPane.addTab(panStr[i], pan[i]);
		}
		makeIconPanel(pan[0]);
		JTextArea text = new JTextArea();
		pan[1].setLayout(new BorderLayout());
		pan[1].add(text);


		/***************************** 분류 시작 *****************************/
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] { "이름순", "형식순", "날짜순" }));
		comboBox.setBounds(370, 25, 96, 29);
		contentPane.add(comboBox);
		/***************************** 분류 종료 *****************************/

		/***************************** HiddenButton 시작 *****************************/
		MediaButton HiddenButton = new MediaButton();
		HiddenButton.setText("숨김파일");
		HiddenButton.setBounds(262, 25, 96, 29);
		contentPane.add(HiddenButton);
		/***************************** HiddenButton 종료 *****************************/

		/***************************** 검색창 시작 *****************************/
		textField = new JTextField();
		textField.setText("검색하기");
		textField.setBounds(478, 25, 96, 29);
		contentPane.add(textField);
		textField.setColumns(10);
		/***************************** 검색창 종료 *****************************/


		controlBtn[0].addActionListener(new UploadActionListener(pan[0]));
		controlBtn[1].addActionListener(new DeleteActionListener(pan[0]));
		controlBtn[2].addActionListener(new DownloadActionListener());



		//		JTextArea text = new JTextArea();
		new FileDrop( System.out, text, new FileDrop.Listener() {   
			public void filesDropped( File[] files ) {   
				for( int i = 0; i < files.length; i++ ) {
					try {
						text.append( files[i].getCanonicalPath() + "\n" );
					} 
					catch( java.io.IOException e ) {}
				}
				Scanner sc = new Scanner(text.getText());
				while(sc.hasNextLine()) {
					String str = sc.nextLine();
					vFile.addElement(new FileInfo(new File(str)));
				} text.setText("");
			}  
		}); 
	}

	void makeIconPanel(JPanel p) {
		p.setLayout(new GridLayout(viewHei, viewWid));

		for(int i = 0 ; i < viewWid*viewHei ; i++) {
			MediaButton btn = new MediaButton();
			vFileBtn.addElement(btn);
			btn.setVisible(false);
			btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MediaButton b = (MediaButton)e.getSource();
					targetFile = b.getFileInfo();
					targetFile.showFileInfo();
				}
			});
			p.add(btn);
		}
	}

	class FileInfo {		
		String address;
		String fileName;
		String date;
		long fileSize;
		char type;
		boolean hide = false;

		FileInfo(File f) {
			address = f.getPath();
			fileName = f.getName();
			date = new SimpleDateFormat("yyyy-MM-dd / HH:mm:ss").format(f.lastModified());
			fileSize = f.length();
			type = searchFileType();
		}
		
		public char searchFileType() {
			String str = "";
			str += fileName.charAt(fileName.length()-3);
			str += fileName.charAt(fileName.length()-2);
			str += fileName.charAt(fileName.length()-1);
			
			switch(str) {
			case "jpg":
			case "png":
			case "bmp": return 'i';
			case "txt":
			case "hwp": return 'd';
			case "mp3":
			case "wav":
			case "mp4": return 'a';
			default: return 'x';
			}
			
			
		}

		public void setAddr(String addr) {
			address = addr;
		}

		public void setDate(String dt) {
			date = dt;
		}

		public void setFileName(String fn) {
			fileName = fn;
		}

		public void setFileSize(long s) {
			fileSize = s;
		}

		public void setHide(boolean h) {
			hide = h;
		}
		
		public void setType(char t) {
			type = t;
		}

		public String getAddr() {
			return address;
		}

		public String getDate() {
			return date;
		}

		public String getFileName() {
			return fileName;
		}

		public long getFileSize() {
			return fileSize;
		}

		public boolean getHide() {
			return hide;
		}
		
		public char getType() {
			return type;
		}

		// 파일의 정보 확인
		public void showFileInfo() {
			System.out.println("절대경로: " + address);
			System.out.println("파 일 명: " + fileName);
			System.out.println("수정날짜: " + date);
			System.out.println("크     기: " + fileSize);
			System.out.println("숨김여부: " + hide);
			System.out.println("파일타입: " + type);
			//			System.out.println("경로:" + selectedFile);
			//			System.out.println("경로:" + selectedFile.getPath());
		}

		public String toString() {
			return fileName;
		}
	}


	class UploadActionListener implements ActionListener {
		JFileChooser chooser;
		JPanel pan;
		UploadActionListener(JPanel p) {
			chooser = new JFileChooser(chooserRoot);
			initFileChooser(chooser);
			pan = p;
		}

		public void actionPerformed(ActionEvent e) {
			int ret = chooser.showOpenDialog(null);
			if (ret != JFileChooser.APPROVE_OPTION) {
				JOptionPane.showMessageDialog(null, "파일을 선택하지 않았습니다", "경고", JOptionPane.WARNING_MESSAGE);
				return;
			}

			File selectedFile = chooser.getSelectedFile();
			vFile.addElement(new FileInfo(selectedFile));
			(new FileInfo(selectedFile)).showFileInfo();
			
			upload();
			refresh(pan);
		}
	}
	
	public void upload() {
		page = (vFile.size()-1)/(viewWid*viewHei) + 1;
		viewCnt = (vFile.size()-1)%(viewWid*viewHei);
		viewCnt++;
	}

	public void refresh(JPanel p) {
		for(int i = 0 ; i < viewWid*viewHei ; i++) {
//			MediaButton btn = new MediaButton();
//			vFileBtn.setElementAt(btn, i);
			vFileBtn.elementAt(i).setVisible(false);
			vFileBtn.elementAt(i).setIcon(null);
			vFileBtn.elementAt(i).setText(null);
//			vFileBtn.elementAt(i).iCon
		}

		for(int i = 0 ; i < viewCnt ; i++) {
			MediaButton btn = vFileBtn.elementAt(i);
			ImageIcon imageIcon;
			Image image, newimg;
			
			switch(vFile.elementAt(i + (page-1)*viewWid*viewHei).type) {
			case 'i':
				imageIcon = new ImageIcon(vFile.elementAt(i + (page-1)*viewWid*viewHei).getAddr()); // load the image to a imageIcon
				image = imageIcon.getImage(); // transform it
				newimg = image.getScaledInstance(120, 120,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way 
				imageIcon = new ImageIcon(newimg);
				btn.setImage(imageIcon, vFile.elementAt(i));
				break;
			case 'd': 
				btn.setDocum(vFile.elementAt(i));
				break;
			case 'a':
				imageIcon = new ImageIcon("audio.jpg"); // load the image to a imageIcon
				image = imageIcon.getImage(); // transform it
				newimg = image.getScaledInstance(120, 120,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way 
				imageIcon = new ImageIcon(newimg);
				btn.setImage(imageIcon, vFile.elementAt(i));
				break;
			default:
				break;
			}
			btn.setVisible(true);
		}
		p.updateUI();
	}

	class DownloadActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			FileInputStream  fin  = null;
			FileOutputStream fout = null;
			int data = 0 ;

			try{
				fin = new FileInputStream(targetFile.getAddr());
				fout = new FileOutputStream(downloadRoot + targetFile.getFileName());//확장명 jpg는 바꾸면 안됨
				while(true) {
					data = fin.read();
					if( data == -1) break;
					fout.write(data); 
				}
				System.out.println();
				System.out.println(targetFile.getFileName() + " 저장 완료");
			}
			catch(IOException e1) {
				System.out.println("파일오픈실패");
				e1.printStackTrace();
			}
			finally {
				try	{
					fin.close();
					fout.close();
				}
				catch(Exception e1) {

				}
			}
			System.out.println("작업종료");
		}
	}

	class DeleteActionListener implements ActionListener {
		JFileChooser chooser;
		JPanel pan;
		DeleteActionListener(JPanel p) {
			chooser = new JFileChooser(chooserRoot);
			initFileChooser(chooser);
			pan = p;
		}

		public void actionPerformed(ActionEvent e) {
			if(targetFile == null) {
				System.out.println("지정파일 없음");
				return;
			}

			System.out.println(targetFile.toString() + " 삭제되었습니다.");
			vFile.remove(targetFile);
			targetFile = null;
			
			delete();
			refresh(pan);
		}
	}
	
	public void delete() {
		viewCnt--;
		if(viewCnt <= 0) {
			if(page <= 1) {
				viewCnt = 0;
			}
			else {
				viewCnt = viewWid*viewHei;
				page--;
			}
		}
	}
	

	// 파일 다이얼로그 선택자
	void initFileChooser(JFileChooser fc) {
		fc.addChoosableFileFilter(new FileNameExtensionFilter("images (JPG, PNG, BMP)", "jpg", "png", "bmp"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Document (HWP, TXT)", "hwp", "txt"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Audio (MP3, WAV, MP4)", "mp3", "wav", "mp4"));
	}

	class MediaButton extends JButton {
		FileInfo info;
		MediaButton() {
			
		}
		
		MediaButton(FileInfo fi) {
			info = fi;
		}

		public void setImage(ImageIcon ic, FileInfo fi) {
			setIcon(ic);
			info = fi;
		}
		
		public void setDocum(FileInfo fi) {
			String str[] = new String[3];
			for(int i = 0 ; i < 3 ; i++)
				str[i] = new String();
			try {
				FileReader reader = new FileReader(fi.getAddr());
				int ch = 0;
				int cnt = 0;
				
				while((ch = reader.read()) != -1) {
					if(ch == '\n') str[cnt] += " ";
					else str[cnt] += (char)ch;
					if(str[cnt].length() > 9) cnt++;
					if(cnt >= 3) break;
				}
				reader.close();
			} catch(FileNotFoundException e) {
				
			} catch(IOException e) {
				
			}
			setText("<html>" + str[0] + "<br>" + str[1] + "<br>" + str[2] + "</html>");
			info = fi;
		}

		public FileInfo getFileInfo() {
			return info;
		}

	}

}
