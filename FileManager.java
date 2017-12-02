/***************************************************************


	1. 이미지가 많을 경우 불러오는 시간으로 인해 오래걸림
	2. 실행하고자 하는 파일의 상위 디렉토리 및 파일명에는 띄어쓰기가 없어야 함



 ****************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mortennobel.imagescaling.*;

public class FileManager extends JFrame {

	private JPanel contentPane;
	JPanel info_panel;
	private JTextField textField;
	Vector<FileInfo> vFile;
	Vector<FileInfo> vImage;
	Vector<FileInfo> vDocum;
	Vector<FileInfo> vAudio;
	Vector<MediaPanel> vFilePan;
	Vector<JLabel> vIfLab;
	FileInfo targetFile;
	JList list;

	JPanel pan[];

	//	int viewWid = 3;
	//	int viewHei = 4;
	int viewWid = 1;
	int viewHei = 5;
	int viewCnt = 0;
	int page = 1;

	String chooserRoot = "G:\\Users\\fkwls\\Desktop\\하스스샷";
	String downloadRoot = "G:\\Users\\fkwls\\Desktop\\FileManagerDownload\\";
	String photoViewer = "mspaint.exe";
	String documentViewer = "notepad.exe";
	String musicPlayer = "F:\\Program Files (x86)\\Windows Media Player\\wmplayer.exe";

	String pattern = "";

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
		vImage = new Vector<FileInfo>();
		vDocum = new Vector<FileInfo>();
		vAudio = new Vector<FileInfo>();

		vFilePan = new Vector<MediaPanel>();
		vIfLab = new Vector<JLabel>();

		contentPane = new JPanel();
		contentPane.setBackground(new Color(57, 174, 169));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		String lr[] = {"<", ">"};
		JButton lrBtn[] = new JButton[lr.length];
		for(int i = 0 ; i < lr.length ; i++) {
			lrBtn[i] = new JButton(lr[i]);
			contentPane.add(lrBtn[i]);
			lrBtn[i].setSize(50, 50);
			lrBtn[i].setLocation(10+i*50, 10);
			lrBtn[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JButton b = (JButton)e.getSource();
					switch(b.getText()) {
					case "<": 
						page--;
						break;
					case ">": 
						page++; 
						break;
					}
					if(page < 1) page = 1;
					else if(page > (vFile.size()-1)/(viewWid*viewHei) + 1) page = (vFile.size()-1)/(viewWid*viewHei) + 1;
					if(page <= vFile.size()/(viewWid*viewHei)) 
						viewCnt = viewWid*viewHei;
					else 
						viewCnt = vFile.size()%(viewWid*viewHei);
					refresh(pan[0]);
				}
			});
		}

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
				System.out.println("vFileBtn.size(): " + vFilePan.size());
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
		formatBtn[2].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("THIS PATTERN: " + pattern);
			}
		});



		/* 파일정보창 */
		info_panel = new JPanel();
		info_panel.setBounds(12, 357, 150, 119);
		contentPane.add(info_panel);
		makeInfoPanel(info_panel);

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


		String panStr[] = {"ICON", "LIST"};
		pan = new JPanel[panStr.length];
		for(int i = 0 ; i < pan.length ; i++) {
			pan[i] = new JPanel();
			tabbedPane.addTab(panStr[i], pan[i]);
		}
		makeIconPanel(pan[0]);
		makeListPanel(pan[1]);

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
		HiddenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LockFrame lf = new LockFrame();
				pattern = lf.getPattern();

			}
		});

		/***************************** HiddenButton 종료 *****************************/

		/***************************** 검색창 시작 *****************************/
		textField = new JTextField();
		textField.setText("검색하기");
		textField.setBounds(478, 25, 96, 29);
		contentPane.add(textField);
		textField.setColumns(10);
		/***************************** 검색창 종료 *****************************/


		controlBtn[0].addActionListener(new UploadActionListener());
		controlBtn[1].addActionListener(new DeleteActionListener());
		controlBtn[2].addActionListener(new DownloadActionListener());

	}

	void makeIconPanel(JPanel p) {
		p.setLayout(new GridLayout(viewHei, viewWid));

		for(int i = 0 ; i < viewWid*viewHei ; i++) {
			MediaPanel mPan = new MediaPanel();
			vFilePan.addElement(mPan);
			mPan.setVisible(false);
			p.add(mPan);
		}
	}

	void makeListPanel(JPanel p) {
		p.setLayout(new BorderLayout());
		list = new JList(vFile);
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				FileInfo fi = (FileInfo)list.getSelectedValue();
				System.out.println(fi);
				if(list.getSelectedIndex() > -1){
					targetFile = fi;
					viewInfoPanel();
					list.updateUI();
				}
			}
		});
		p.add(new JScrollPane(list));
	}

	void makeInfoPanel(JPanel p) {
		String str[] = {"절대경로", "파 일 명", "수정날짜", "크     기", "숨김여부", "파일타입"};
		p.setLayout(new GridLayout(str.length, 2));
		for(int i = 0 ; i < str.length ; i++) {
			JLabel la = new JLabel();
			p.add(new JLabel(str[i]));
			p.add(la);
			vIfLab.addElement(la);
		}
	}

	void viewInfoPanel() {
		vIfLab.elementAt(0).setText(targetFile == null ? "" : targetFile.getAddr());
		vIfLab.elementAt(1).setText(targetFile == null ? "" : targetFile.getFileName());
		vIfLab.elementAt(2).setText(targetFile == null ? "" : targetFile.getDate());
		vIfLab.elementAt(3).setText(targetFile == null ? "" : targetFile.getSize());
		vIfLab.elementAt(4).setText(targetFile == null ? "" : (targetFile.getHide()? "숨김": "공개"));
		String type = "";
		switch(targetFile.getType()) {
		case 'i': type = "IMAGE"; break;
		case 'd': type = "DOCUMENT"; break;
		case 'a': type = "AUDIO"; break;
		default: type = "UNKNOWN"; break;
		}
		vIfLab.elementAt(5).setText(targetFile == null ? "" : type);
		info_panel.updateUI();
	}

	class FileInfo {		
		String address;
		String fileName;
		String date;
		int fileSize;
		int size;
		String unit;
		char type;
		boolean hide = false;

		FileInfo(File f) {
			address = f.getPath();
			fileName = f.getName();
			date = new SimpleDateFormat("yyyy-MM-dd / HH:mm:ss").format(f.lastModified());
			fileSize = (int)f.length();
			sizeComp();
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

		// 파일 사이즈 짧게 조정
		public void sizeComp() {
			int temp = fileSize;
			int s = 0;
			int u = 0;

			while(temp > 0) {
				if(temp/1024 > 0) u++;
				else s = temp;
				temp /= 1024;
			}
			size = s;
			switch(u) {
			case 0: unit = "Byte"; break;
			case 1: unit = "KB"; break;
			case 2: unit = "MB"; break;
			default: unit = "GB"; break;
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

		public void setFileSize(int s) {
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

		public String getSize() {
			return Integer.toString(size) + unit;
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
		}

		public String toString() {
			return fileName;
		}
	}


	class UploadActionListener implements ActionListener {
		JFileChooser chooser;

		UploadActionListener() {
			chooser = new JFileChooser(chooserRoot);
			initFileChooser(chooser);
		}

		public void actionPerformed(ActionEvent e) {
			int ret = chooser.showOpenDialog(null);
			if (ret != JFileChooser.APPROVE_OPTION) {
				JOptionPane.showMessageDialog(null, "파일을 선택하지 않았습니다", "경고", JOptionPane.WARNING_MESSAGE);
				return;
			}

			File selectedFile = chooser.getSelectedFile();
			vFile.addElement(new FileInfo(selectedFile));
			//			(new FileInfo(selectedFile)).showFileInfo();

			fileIO();
			refresh(pan[0]);
		}
	}

	public void fileIO() {
		page = (vFile.size()-1)/(viewWid*viewHei) + 1;
		viewCnt = (vFile.size()-1)%(viewWid*viewHei);
		viewCnt++;
	}

	public void refresh(JPanel p) {
		for(int i = 0 ; i < viewWid*viewHei ; i++) {
			//			MediaButton btn = new MediaButton();
			//			vFileBtn.setElementAt(btn, i);
			vFilePan.elementAt(i).setVisible(false);
			vFilePan.elementAt(i).mBtn.setIcon(null);
			vFilePan.elementAt(i).mBtn.setText(null);
			//			vFileBtn.elementAt(i).iCon
		}

		for(int i = 0 ; i < viewCnt ; i++) {
			MediaPanel mPan = vFilePan.elementAt(i);
			mPan.setMediaPanel(i);
			mPan.setVisible(true);
		}
		p.updateUI();
	}

	class DownloadActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(targetFile == null) {
				System.out.println("지정된 파일이 없습니다.");
				return;
			}

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

		DeleteActionListener() {
			chooser = new JFileChooser(chooserRoot);
			initFileChooser(chooser);
		}

		public void actionPerformed(ActionEvent e) {
			if(targetFile == null) {
				System.out.println("지정파일 없음");
				return;
			}

			System.out.println(targetFile.toString() + " 삭제되었습니다.");
			vFile.remove(targetFile);
			targetFile = null;

			fileIO();
			refresh(pan[0]);
		}
	}


	// 파일 다이얼로그 선택자
	void initFileChooser(JFileChooser fc) {
		fc.addChoosableFileFilter(new FileNameExtensionFilter("images (JPG, PNG, BMP)", "jpg", "png", "bmp"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Document (HWP, TXT)", "hwp", "txt"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Audio (MP3, WAV, MP4)", "mp3", "wav", "mp4"));
	}

	class MediaPanel extends JPanel {
		FileInfo info;
		MediaButton mBtn;
		ImageIcon imageIcon;
		Image image, newimg;
		JLabel la;
		MediaPanel() {
			setLayout(new BorderLayout());

			mBtn = new MediaButton();
			la = new JLabel();

			add(mBtn, BorderLayout.CENTER);
			add(la, BorderLayout.SOUTH);
		}

		MediaPanel(int num) {
			setMediaPanel(num);
			info = vFile.elementAt(num);
			la.setText(info.getFileName());
		}

		public void setMediaPanel(int num) {
			switch(vFile.elementAt(num + (page-1)*viewWid*viewHei).type) {
			case 'i':
//				createImageThumbnail(vFile.elementAt(num + (page-1)*viewWid*viewHei).getAddr(),
//						vFile.elementAt(num + (page-1)*viewWid*viewHei).getFileName());
				imageIcon = new ImageIcon(vFile.elementAt(num + (page-1)*viewWid*viewHei).getAddr()); // load the image to a imageIcon
				image = imageIcon.getImage(); // transform it
				newimg = image.getScaledInstance(120, 120,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way 
				imageIcon = new ImageIcon(newimg);
				mBtn.setImage(imageIcon, vFile.elementAt(num));
				break;
			case 'd': 
				mBtn.setDocum(vFile.elementAt(num + (page-1)*viewWid*viewHei));
				break;
			case 'a':
				imageIcon = new ImageIcon("audio.jpg"); // load the image to a imageIcon
				image = imageIcon.getImage(); // transform it
				newimg = image.getScaledInstance(120, 120,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way 
				imageIcon = new ImageIcon(newimg);
				mBtn.setImage(imageIcon, vFile.elementAt(num + (page-1)*viewWid*viewHei));
				break;
			default:
				mBtn.setText("?");
				break;
			}
			info = vFile.elementAt(num);
			la.setText(info.getFileName());

		}

	}

	class MediaButton extends JButton {
		FileInfo info;
		MediaButton() {
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MediaButton b = (MediaButton)e.getSource();

					if(b.getFileInfo() == targetFile) {
						Runtime r1 = Runtime.getRuntime();
						switch(info.getType()) {
						case 'i':
							try {
								//프로그램명 매개변수  넣어주면 실행
								//파일 경로명 넣어주면 경로명에 있는 파일이 노트패드로 실행
								r1.exec(photoViewer + " " + targetFile.getAddr()); 
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							break;
						case 'd':
							try {
								//프로그램명 매개변수  넣어주면 실행
								//파일 경로명 넣어주면 경로명에 있는 파일이 노트패드로 실행
								r1.exec(documentViewer + " " + targetFile.getAddr()); 
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							break;
						case 'a':
							try {
								//프로그램명 매개변수  넣어주면 실행
								//파일 경로명 넣어주면 경로명에 있는 파일이 노트패드로 실행
								r1.exec(musicPlayer + " " + targetFile.getAddr()); 
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							break;
						}
					}

					targetFile = b.getFileInfo();
					targetFile.showFileInfo();
					viewInfoPanel();
					info_panel.updateUI();
				}
			});
		}

		MediaButton(FileInfo fi) {
			info = fi;
		}

		public void setImage(ImageIcon ic, FileInfo fi) {
			setIcon(ic);
			info = fi;
		}

		public void setAudio(ImageIcon ic, FileInfo fi) {
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

	/**
	 * 이미지 섬네일 생성(기본 사이즈:150×150)
	 * @param srcPath 원본 파일 경로
	 * @param srcFileNm 원본 파일 명
	 * @return
	 */
	public static boolean createImageThumbnail(String srcPath, String srcFileNm) {
		return createImageThumbnail(srcPath, srcFileNm, 150, 150);
	}
	
	public static boolean createImageThumbnail(String srcPath, String srcFileNm, int width, int height) {

		if( srcPath==null || srcFileNm==null ) {
			return false;
		}

		try {
			BufferedImage buffImage = ImageIO.read(new File(srcPath+srcFileNm));

			ResampleOp resampleOp = new ResampleOp(width, height);
			resampleOp.setUnsharpenMask( AdvancedResizeOp.UnsharpenMask.Soft );
			BufferedImage rescaledImage = resampleOp.filter(buffImage, null);

			String destPath = "G:\\Users\\fkwls\\Desktop\\thumbnail\\thumb_" + srcFileNm;
			File destFile = new File(destPath);

			ImageIO.write(rescaledImage, "png", destFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
