/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.schemaupdate;

import java.util.EnumSet;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import org.hibernate.testing.orm.junit.JiraKey;
import org.hibernate.testing.orm.junit.BaseUnitTest;
import org.hibernate.testing.orm.junit.RequiresDialect;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Andrea Boriero
 */
@JiraKey(value = "HHH-14835")
@BaseUnitTest
@RequiresDialect(value = SQLServerDialect.class, majorVersion = 12)
public class SchemaExportSqlServerWithSequenceDefaultSchemaCatalog {
	protected ServiceRegistry serviceRegistry;
	protected MetadataImplementor metadata;

	@Test
	public void shouldCreateIndex() {
		SchemaExport schemaExport = new SchemaExport();
		schemaExport.create( EnumSet.of( TargetType.DATABASE, TargetType.STDOUT ), metadata );
		assertThat( schemaExport.getExceptions().size(), is( 0 ) );
	}

	@BeforeEach
	public void setUp() {
		serviceRegistry = ServiceRegistryUtil.serviceRegistryBuilder()
				.applySetting( Environment.DEFAULT_SCHEMA, "dbo" )
				.applySetting( Environment.DEFAULT_CATALOG, "hibernate_orm_test" )
				.build();
		metadata = (MetadataImplementor) new MetadataSources( serviceRegistry )
				.addAnnotatedClass( MyEntity.class )
				.buildMetadata();

		System.out.println( "********* Starting SchemaExport for START-UP *************************" );
		new SchemaExport().create( EnumSet.of( TargetType.DATABASE, TargetType.STDOUT ), metadata );
		System.out.println( "********* Completed SchemaExport for START-UP *************************" );
	}


	@AfterEach
	public void tearDown() {
		System.out.println( "********* Starting SchemaExport (drop) for TEAR-DOWN *************************" );
		new SchemaExport().drop( EnumSet.of( TargetType.DATABASE, TargetType.STDOUT ), metadata );
		System.out.println( "********* Completed SchemaExport (drop) for TEAR-DOWN *************************" );

		StandardServiceRegistryBuilder.destroy( serviceRegistry );
		serviceRegistry = null;
	}


	@Entity
	@Table(name = "MyEntity")
	public static class MyEntity {
		private int id;

		@Id
		@GeneratedValue
		public int getId() {
			return this.id;
		}

		public void setId(final int id) {
			this.id = id;
		}
	}
}
