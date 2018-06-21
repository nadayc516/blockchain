package com.example;

import org.bson.Document;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import com.mongodb.client.MongoDatabase;

public class mongodconfig {
	static MongoClientURI connectionString = new MongoClientURI("mongodb://localhost/blockchain");
	static MongoClient mongoClient = new MongoClient(connectionString);

	public static final MongoDatabase DB = mongoClient.getDatabase("blockchain");
}
