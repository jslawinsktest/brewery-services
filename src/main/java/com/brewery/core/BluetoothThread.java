package com.brewery.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.bluetooth.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.brewery.model.Batch;
import com.brewery.model.Measurement;
import com.brewery.model.Message;
import com.brewery.model.Sensor;
import com.brewery.model.SensorData;
import com.brewery.service.DataService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Scope("prototype")
public class BluetoothThread implements Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger( BluetoothThread.class );

	// public static MessageQueue blueToothQueue = new MessageQueue( 100, "BTQueue" );
	
	private static ArrayBlockingQueue<Message> blueToothQueue = new ArrayBlockingQueue<Message>( 100 ); 
	
    private DataService dataService;
    @Autowired
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }
	
    @Value("${blueTooth.scanSeconds}")
    private int scanSeconds;
    
    @Override
    public void run() {
        LOG.info("Running BluetoothThread");
        while( true ) {
			try {
				Thread.sleep( 1000 * scanSeconds ); 

				List<Sensor> sensors= dataService.getEnabledSensors();
				Thread.sleep(500);
				
				Message message = null;
				if( !blueToothQueue.isEmpty() ) {
					message = blueToothQueue.remove();
					LOG.info("Message: " + message );						
				}

				for( Sensor sensor : sensors ) {
					LOG.info( "Active Sensor: " + sensor );
					LOG.info("Connecting to " + sensor.getUrl() );

					StreamConnection streamConnection;
					streamConnection = (StreamConnection)Connector.open( sensor.getUrl() );
					Thread.sleep(500);

					if( message != null ) {
						String target = message.getTarget();
						if( target != null ) {
							if( target.equals( sensor.getName() ) ){
								LOG.info("Sending Message: " + message );								
								OutputStream outStream=streamConnection.openOutputStream();
								PrintWriter pWriter=new PrintWriter(new OutputStreamWriter(outStream));
								pWriter.write( message.getData() + "\n\r");
								pWriter.flush();
				    	        outStream.close();
							}
						}
					}
					
    		        ObjectMapper objectMapper = new ObjectMapper();    		        
    		        InputStream inStream=streamConnection.openInputStream();
    		        try {
				        //read response
				        BufferedReader bReader2=new BufferedReader(new InputStreamReader(inStream));
				        String lineRead=bReader2.readLine();
				        LOG.info( "Bluetooth Data: " + lineRead);
				        if( lineRead != null ) {
					        SensorData sensorData = objectMapper.readValue(lineRead, SensorData.class);
					        LOG.info( "SensorData: " + sensorData );
					        Measurement measurement = new Measurement();
					        measurement.setId( 0L );
					    	measurement.setMeasurementTime( new Date() );
					    	measurement.setBatch( sensor.getBatch() );
					    	measurement.setProcess( sensor.getProcess() );
					    	measurement.setType( sensor.getMeasureType() );
					    	measurement.setValueNumber( sensorData.getTemperature() );
					    	measurement.setValueText( "{\"target\":" + sensorData.getTarget() + "}");
					        dataService.saveMeasurement( measurement );
				        }
    				} catch( Exception e ) {
    					e.printStackTrace();
    				}
			        inStream.close();
			        
	    	        streamConnection.close();
				}    			        
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch( Exception e ) {
				e.printStackTrace();
			}	
        }
    }
    
    static public void sendMessage( Message message ) {
    	blueToothQueue.add( message );
    }
}