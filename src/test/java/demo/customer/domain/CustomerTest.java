package demo.customer.domain;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Slice test does not use CustomerConfig
// Does this change the behavior to NOT replace the datasource?
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CustomerTest {

	@Autowired
	private CustomerRepository customerRepository;

	@Test
	void save() {
		Customer customer = new Customer("John", "Smith");
		assertThat(customer.getId()).isNull();
		this.customerRepository.save(customer);
		assertThat(customer.getId()).isNotNull();
	}

}
