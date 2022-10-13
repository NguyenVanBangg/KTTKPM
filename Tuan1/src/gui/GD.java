package gui;

import java.awt.EventQueue;
import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.BasicConfigurator;

import data.Person;
import helper.XMLConvert;
import java.util.Date;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.log4j.BasicConfigurator;
import data.Person;
import javax.swing.JTextPane;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.Properties;
import java.awt.event.ActionEvent;
import java.awt.Label;
import javax.swing.JLabel;
import java.awt.Color;

public class GD extends JFrame {

	private JPanel contentPane;
	static JTextArea historyChat;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GD frame = new GD();
					frame.setVisible(true);
					
					frame.setLocationRelativeTo(null);  
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		try {
			//thiết lập môi trường cho JMS
			BasicConfigurator.configure();
			//thiết lập môi trường cho JJNDI
			Properties settings=new Properties();
			settings.setProperty(Context.INITIAL_CONTEXT_FACTORY,
					"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
			settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
			//tạo context
			Context ctx=new InitialContext(settings);
			//lookup JMS connection factory
			Object obj=ctx.lookup("ConnectionFactory");
			ConnectionFactory factory=(ConnectionFactory)obj;
			//lookup destination
			Destination destination
			=(Destination) ctx.lookup("dynamicQueues/tranthidet");
			//tạo connection
			Connection con=factory.createConnection("admin","admin");
			//nối đến MOM
			con.start();
			//tạo session
			Session session=con.createSession(
					/*transaction*/false,
					/*ACK*/Session.CLIENT_ACKNOWLEDGE
					);
			//tạo consumer
			MessageConsumer receiver = session.createConsumer(destination);
			//blocked-method for receiving message - sync
			//receiver.receive();
			//Cho receiver lắng nghe trên queue, chừng có message thì notify - async
			System.out.println("Tý was listened on queue...");
			receiver.setMessageListener(new MessageListener() {
				@Override
				//có message đến queue, phương thức này được thực thi
				public void onMessage(Message msg) {
					//msg là message nhận được
					try {
						if(msg instanceof TextMessage){
							TextMessage tm=(TextMessage)msg;
							String txt=tm.getText();
							historyChat.setText(txt);
							msg.acknowledge();//gửi tín hiệu ack
						}
						else if(msg instanceof ObjectMessage){
							ObjectMessage om=(ObjectMessage)msg;
							System.out.println(om);
						}
						//others message type....
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Create the frame.
	 */
	public GD() {
		setTitle("ChatBox");
		
		setBounds(100, 100, 612, 447);
		contentPane = new JPanel();
		contentPane.setBackground(Color.PINK);
		  
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.PINK);
		panel.setBounds(10, 11, 578, 399);
		contentPane.add(panel);
		panel.setLayout(null);
		
	 historyChat = new JTextArea();
		historyChat.setEditable(false);
		historyChat.setBounds(0, 25, 578, 306);
		panel.add(historyChat);
		
		JTextField inputChat = new JTextField();
		inputChat.setBounds(0, 357, 467, 31);
		panel.add(inputChat);
		inputChat.setColumns(10); 
		JButton btnNewButton = new JButton("Gửi");
	
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					//config environment for JMS
					BasicConfigurator.configure();
			//config environment for JNDI
					Properties settings = new Properties();
					settings.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
					settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
			//create context
					Context ctx = new InitialContext(settings);
			//lookup JMS connection factory
					ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
			//lookup destination. (If not exist-->ActiveMQ create once)
					Destination destination = (Destination) ctx.lookup("dynamicQueues/thanthidet");
			//get connection using credential
					Connection con = factory.createConnection("admin", "admin");
			//connect to MOM
					con.start();
			//create session
					Session session = con.createSession(/* transaction */false, /* ACK */Session.AUTO_ACKNOWLEDGE);
			//create producer
					MessageProducer producer = session.createProducer(destination);
					
			//create text message
					
					Message msg = session.createTextMessage(inputChat.getText().toString());
					inputChat.setText("");
					producer.send(msg);
					Person p = new Person(1001, "Thân Thị Đẹt", new Date());
					String xml = new XMLConvert<Person>(p).object2XML(p);
					msg = session.createTextMessage(xml);
					producer.send(msg);
					
			//shutdown connection
					session.close();
					con.close();
					System.out.println("Finished...");
				
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		});
		
		btnNewButton.setBounds(477, 361, 89, 23);
		panel.add(btnNewButton);
		
		JLabel lblNewLabel = new JLabel("Cửa sổ tin nhắn");
		lblNewLabel.setBounds(0, 0, 124, 23);
		panel.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Nhập tin nhắn");
		lblNewLabel_1.setBounds(0, 334, 124, 23);
		panel.add(lblNewLabel_1);
	}
}
