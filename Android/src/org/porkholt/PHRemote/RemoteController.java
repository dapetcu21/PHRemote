package org.porkholt.PHRemote;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.ListIterator;

public class RemoteController {
	public static final String DEFAULT_HOST = "255.255.255.255";
	public static final String DEFAULT_PORT = "2221";
	public static final boolean DEFAULT_USE_ACCEL = true;
	public static final boolean DEFAULT_USE_TOUCH = true;
	public static final boolean DEFAULT_GROUP_PACKS = false;

	public static final int STATE_DOWN = 0;
	public static final int STATE_UP = 1;
	public static final int STATE_CANCELLED = 2;
	public static final int STATE_MOVED = 3;
	
	
	private String hostString = DEFAULT_HOST;
	private String portString = DEFAULT_PORT;
	private boolean running = false;
	private boolean useAccel = DEFAULT_USE_ACCEL;
	private boolean useTouch = DEFAULT_USE_TOUCH;
	private boolean group = DEFAULT_GROUP_PACKS;
	private int width = 0;
	private int height = 0;
	private DatagramChannel sock = null;
	
	public void setUseAccelerometer(boolean b) {
		useAccel = b;
		System.out.println("PHR: " + b);
	}
	
	public void setUseTouchEvents(boolean b) {
		useTouch = b;
	}
	
	public void setGroupPackets(boolean b) {
		group = b;
	}
	
	public boolean getUseAccelerometer() {
		return useAccel;
	}
	
	public boolean getUseTouchEvents() {
		return useTouch;
	}
	
	public boolean getGroupPackets() {
		return group;
	}
	
	public void setHost(String s) {
		hostString = s;
		System.out.println("PHR: " + s);
	}
	
	public void setPort(String s) {
		portString = s;
	}
	
	public String getHost() {
		return hostString;
	}
	
	public String getPort() {
		return portString;
	}
	
	public void setWidth(int v) {
		width = v;
	}
	
	public void setHeight(int v) {
		height = v;
	}
	
	public int getWidth() { 
		return width;
	}
	
	public int getHeight() { 
		return height;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void start() throws Exception {
		stop();
		int port = Integer.parseInt(portString);
		InetSocketAddress addr = new InetSocketAddress(hostString, port);
		sock = DatagramChannel.open();
		sock.connect(addr);
		running = true;
	}
	
	public void stop() {
		if (!running) return;
		running = false;
		try {
			sock.close();
		} catch (IOException e) {
			//do nothing
		}
		sock = null;
	}
	
	private class AccelPacket {
		public float x,y,z;
		public AccelPacket(float _x, float _y, float _z) {
			x = _x;
			y = _y;
			z = _z;
		}
	}
	
	private class TouchPacket {
		public int state, id, x,y, w,h;
		public TouchPacket(int st, int _id, int _x, int _y, int width, int height) {
			state = st;
			id = _id;
			x = _x;
			y = _y;
			w = width;
			h = height;
		}
	}
	
	private AccelPacket accelPack = null;
	private LinkedList<TouchPacket> packetList = new LinkedList<TouchPacket>();
	
	private void writeToBuffer32(byte[] d, int o, int n)
	{
		d[0+o] = (byte) ((n & 0xFF000000L) >> 24);
		d[1+o] = (byte) ((n & 0x00FF0000L) >> 16);
		d[2+o] = (byte) ((n & 0x0000FF00L) >> 8);
		d[3+o] = (byte) ( n & 0x000000FFL);
	}
	
	private void writeToBuffer64(byte[] d, int o, long n)
	{

		d[0+o] = (byte) ((n & 0xFF00000000000000L) >> 56);
		d[1+o] = (byte) ((n & 0x00FF000000000000L) >> 48);
		d[2+o] = (byte) ((n & 0x0000FF0000000000L) >> 40);
		d[3+o] = (byte) ((n & 0x000000FF00000000L) >> 32);
		d[4+o] = (byte) ((n & 0x00000000FF000000L) >> 24);
		d[5+o] = (byte) ((n & 0x0000000000FF0000L) >> 16);
		d[6+o] = (byte) ((n & 0x000000000000FF00L) >> 8);
		d[7+o] = (byte) ( n & 0x00000000000000FFL);
	}
	
	public void sendQueuedPackets() throws Exception
	{
		int size = 1;
		int w = 0;
		int h = 0;
		
		for (ListIterator<TouchPacket> it = packetList.listIterator(); it.hasNext();)
		{
			TouchPacket p = it.next();
			size+=2*5+4*3+1;
			if (p.w != w || p.h != h)
			{
				w = p.w;
				h = p.h;
				size+=(4+2)*2;
			}
		}
		if (accelPack != null)
			size+=8*3+2;
		
		if (size == 1)
			return;
		
		byte[] d = new byte[size];
		int off = 1;
		d[0] = (byte) 0xAC;
		
		if (accelPack != null)
		{
			d[off++] = (byte)0x01;
			d[off++] = (byte)(8*3);
			writeToBuffer64(d,off,(long)(accelPack.x*0x100000)); off+=8;
			writeToBuffer64(d,off,(long)(accelPack.y*0x100000)); off+=8;
			writeToBuffer64(d,off,(long)(accelPack.z*0x100000)); off+=8;
		}
		
		w = h = 0;
		
		for (ListIterator<TouchPacket> it = packetList.listIterator(); it.hasNext();)
		{
			TouchPacket p = it.next();
			if (p.w != w || p.h != h)
			{
				w = p.w;
				h = p.h;
				d[off++] = (byte)0x06;
				d[off++] = (byte)4;
				writeToBuffer32(d,off,w); off+=4;
				d[off++] = (byte)0x07;
				d[off++] = (byte)4;
				writeToBuffer32(d,off,h); off+=4;
			}
			
			d[off++] = (byte)0x02;
			d[off++] = (byte)4;
			writeToBuffer32(d,off,p.id); off+=4;
			
			d[off++] = (byte)0x03;
			d[off++] = (byte)1;
			d[off++] = (byte)p.state;

			d[off++] = (byte)0x04;
			d[off++] = (byte)4;
			writeToBuffer32(d, off, p.x); off+=4;
			
			d[off++] = (byte)0x05;
			d[off++] = (byte)4;
			writeToBuffer32(d, off, p.y); off+=4;
			
			d[off++] = (byte)0x08;
			d[off++] = (byte)0;
			
		}
		
		accelPack = null;
		packetList.clear();
		
		ByteBuffer buffer = ByteBuffer.wrap(d,0,off);
		sock.write(buffer);
	}
	
	public void queuePacket(Object pack) throws Exception 
	{
		if (pack instanceof AccelPacket)
			accelPack = (AccelPacket)pack;
		else if (pack instanceof TouchPacket) {
			packetList.add((TouchPacket)pack);
		} else
			throw new Exception("invalid packet class");
	}
	
	public void sendAccelerometerPacket(float x, float y, float z) throws Exception  
	{
		if (!running) return;
		if (useAccel)
		{
			System.out.println("PHR: accel: x:" + x + " y:" + y + " z:" + z);
			queuePacket(new AccelPacket(x, y, z));
		}
		sendQueuedPackets();
	}
	
	
	public void sendTouchEventPacket(int id, int state, int x, int y) throws Exception
	{
		if (!running) return;
		if (!useTouch) return;
		System.out.println("PHR: touch: id:" + id + " phase:" + state + " x:" + x + " y:" + y);
		queuePacket(new TouchPacket(state, id, x, y, width, height));
		if (!group)
			sendQueuedPackets();
	}
}
