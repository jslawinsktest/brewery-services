package com.brewery;

import com.brewery.model.Style;
import com.brewery.model.Batch;
import com.brewery.model.Process;
import com.brewery.model.Measurement;
import com.brewery.model.MeasureType;
import com.brewery.repository.StyleRepository;
import com.brewery.repository.BatchRepository;
import com.brewery.repository.ProcessRepository;
import com.brewery.repository.MeasurementRepository;
import com.brewery.repository.MeasureTypeRepository;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BreweryApplication implements CommandLineRunner {

	private StyleRepository styleRepository;
	@Autowired
	public void styleRepository( StyleRepository styleRepository ) {
		this.styleRepository = styleRepository;
	}

	private BatchRepository batchRepository;
	@Autowired
	public void batchRepository( BatchRepository batchRepository ) {
		this.batchRepository = batchRepository;
	}

	private ProcessRepository processRepository;
	@Autowired
	public void  processRepository( ProcessRepository processRepository ) {
		this.processRepository = processRepository;
	}
	
	private MeasurementRepository measurementRepository;
	@Autowired
	public void  measurementRepository( MeasurementRepository measurementRepository ) {
		this.measurementRepository = measurementRepository;
	}
	
	private MeasureTypeRepository measureTypeRepository;
	@Autowired
	public void  measureTypeRepository( MeasureTypeRepository measureTypeRepository ) {
		this.measureTypeRepository = measureTypeRepository;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(BreweryApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {

		Style testStyle = new Style( "IPA", "18a", "Hoppy" );
		styleRepository.save( testStyle );
		
		Batch testBatch = new Batch( "Joe's IPA", "Old School IPA", testStyle, new Date() );
		batchRepository.save( testBatch );
		
		Process process = new Process( "FRM", "Fermentation" );
		processRepository.save( process );
		
		MeasureType measureType = new MeasureType( "TMP", "Temprature" );
		measureTypeRepository.save(measureType);
		
		Measurement measurement = new Measurement( 70.3, null, testBatch, process, measureType, new Date() );
		measurementRepository.save( measurement );
		
		measurement = new Measurement( 70.5, null, testBatch, process, measureType, new Date() );
		measurementRepository.save( measurement );
		
	}
}