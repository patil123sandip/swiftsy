package com.example.swiftsy_app.config;

import com.example.swiftsy_app.domain.Customer;
import com.example.swiftsy_app.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    public DataSeeder(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (customerRepository.count() == 0) {
            Customer c1 = new Customer();
            c1.setName("Acme Corp Logistics");
            c1.setAddress("123 Industrial Way, Mumbai, 400001");
            c1.setGstNumber("27AADCB2230M1Z2");

            Customer c2 = new Customer();
            c2.setName("Global Imports Ltd");
            c2.setAddress("789 Harbor Street, Chennai, 600001");
            c2.setGstNumber("33AAACG4410R1Z2");

            Customer c3 = new Customer();
            c3.setName("Velocity Export Solutions");
            c3.setAddress("45 Tech Park, Delhi, 110001");
            c3.setGstNumber("07AAECV5520M1Z2");

            customerRepository.saveAll(Arrays.asList(c1, c2, c3));
        }
    }
}
