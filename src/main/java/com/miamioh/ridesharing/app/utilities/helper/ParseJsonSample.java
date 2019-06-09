package com.miamioh.ridesharing.app.utilities.helper;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.*;

import java.io.File;

public class ParseJsonSample {

	public static void main(String[] args) throws Exception {
		System.out.println("Inside the right place");
		double[] distanceAndTime = new double[2];
		JsonFactory f = new MappingJsonFactory();
		JsonParser jp = f.createJsonParser(new File(args[0]));
		JsonToken current;
		System.out.println(jp);
		current = jp.nextToken();
		System.out.println(current);
		if (current != JsonToken.START_OBJECT) {
			System.out.println("Error: root should be object: quiting.");
			return;
		}
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String fieldName = jp.getCurrentName();
			// move from field name to field value
			System.out.println(fieldName);
			current = jp.nextToken();
			if (fieldName.equals("route")) {
				System.out.println("inside route");
				current = jp.nextToken();
			} else if (fieldName.equals("summary")) {
				System.out.println("current" + current);

				while (jp.nextToken() != JsonToken.END_OBJECT) {
					String fieldName2 = jp.getCurrentName();
					System.out.println(fieldName2);
					if (fieldName2.equalsIgnoreCase("distance")) {
						fieldName2 = jp.getText();
						System.out.println(fieldName2);
						System.out.println("here" + jp.getText());
						
					
						// String val = jp.getText();
						 distanceAndTime[0]=Double.parseDouble(fieldName2);
					}
					if (fieldName2.equalsIgnoreCase("trafficTime")) {
						System.out.println("b4"+fieldName2);
						fieldName2 = jp.getText();
						System.out.println(fieldName2);
						// distanceAndTime[1]=jp.getDoubleValue();
						break;
					}
				}

			} else if (!fieldName.equals("response")) {
				System.out.println("Unprocessed property: " + fieldName);
				jp.skipChildren();
			}
		}
		System.out.println(distanceAndTime[0] + " : " + distanceAndTime[1]);
	}
}