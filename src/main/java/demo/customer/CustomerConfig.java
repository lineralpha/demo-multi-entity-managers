package demo.customer;

import javax.persistence.EntityManagerFactory;

import com.zaxxer.hikari.HikariDataSource;
import demo.customer.domain.Customer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "customerEntityManager",
		               transactionManagerRef = "customerTransactionManager",
                       basePackageClasses = Customer.class)
public class CustomerConfig {

	private final PersistenceUnitManager persistenceUnitManager;

	public CustomerConfig(ObjectProvider<PersistenceUnitManager> persistenceUnitManager) {
		this.persistenceUnitManager = persistenceUnitManager.getIfAvailable();
	}

    /**
     * Uses {@code app.customer.jpa.*} to set properties equivalent to {@code spring.jpa.*} in
     * Spring auto configuration.
     */
	@Bean
	@ConfigurationProperties("app.customer.jpa")
	public JpaProperties customerJpaProperties() {
		return new JpaProperties();
	}

    /**
     * {@code app.customer.datasource.*} provides properties equivalent to
     * {@code spring.datasource.*} properties for setting the data source.
     */
	@Bean
	@Primary
	@ConfigurationProperties("app.customer.datasource")
	public DataSourceProperties customerDataSourceProperties() {
		return new DataSourceProperties();
	}

    /**
     * {@code app.customer.datasource.properties.*} provides additional settings
     * passed to the datasource. Same settings as in {@code spring.datasource.hikari.*}.
     */
	@Bean
	@Primary
	@ConfigurationProperties(prefix = "app.customer.datasource.properties")
	public HikariDataSource customerDataSource() {
		return customerDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
	}

    /**
     * The {@code EntityManager} that is created using {@code app.customer.jpa.*}
     * properties and is bound to the customer datasource created using
     * {@code app.customer.datasource.*} properties.
     */
	@Bean
	public LocalContainerEntityManagerFactoryBean customerEntityManager(JpaProperties customerJpaProperties) {
        // use "app.customer.jpa.*" properties to instantiate the builder
		EntityManagerFactoryBuilder builder = createEntityManagerFactoryBuilder(customerJpaProperties);
		return builder.dataSource(customerDataSource()).packages(Customer.class).persistenceUnit("customersDs").build();
	}

	@Bean
	@Primary
	public JpaTransactionManager customerTransactionManager(EntityManagerFactory customerEntityManager) {
		return new JpaTransactionManager(customerEntityManager);
	}

	private EntityManagerFactoryBuilder createEntityManagerFactoryBuilder(JpaProperties customerJpaProperties) {
        // "app.customer.jpa.*" properties are used to create the jpa vendor adapter
		JpaVendorAdapter jpaVendorAdapter = createJpaVendorAdapter(customerJpaProperties);
        // additional "app.customer.jpa.properties.*" are passed to the builder
		return new EntityManagerFactoryBuilder(jpaVendorAdapter, customerJpaProperties.getProperties(),
				this.persistenceUnitManager);
	}

	private JpaVendorAdapter createJpaVendorAdapter(JpaProperties jpaProperties) {
        // use the "app.customer.jpa.*" properties to instantiate the vendor adapter
		AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
		adapter.setShowSql(jpaProperties.isShowSql());
		if (jpaProperties.getDatabase() != null) {
			adapter.setDatabase(jpaProperties.getDatabase());
		}
		if (jpaProperties.getDatabasePlatform() != null) {
			adapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());
		}
		adapter.setGenerateDdl(jpaProperties.isGenerateDdl());
		return adapter;
	}

}
