package pizza;

import org.h2.jdbcx.JdbcDataSource;
import pizza.customer.CustomerService;
import pizza.order.OrderService;
import pizza.product.ProductJdbcDao;
import pizza.product.ProductService;

import javax.sql.DataSource;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public class PizzaApp {

    private final CustomerService customService;
    private final ProductService productService;
    private final OrderService orderService;

    public PizzaApp() {
        this.customService = new CustomerService();
        this.productService = new ProductService(new ProductJdbcDao(getDataSource()));
        this.orderService = new OrderService(this.customService, this.productService);
        new SampleDataLoader.SmallDataLoader(this.productService, this.customService).run();
    }

    ProductService getProductService() {
        return this.productService;
    }

    CustomerService getCustomerService() {
        return this.customService;
    }

    OrderService getOrderService() {
        return this.orderService;
    }

    DataSource getDataSource() {
        // start a H2 database instance
        new H2Launcher().run();

        // use a H2 DataSource implementation
        var dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:tcp://localhost:9092/~/training.spring-boot.pizza");

        // run a script to set up the database schema (=tables)
        new H2ScriptRunner(dataSource).run();

        // return the data source for others to work with
        return dataSource;
    }

    public static void main(String[] args) {
        // launch pizzeria
        var app = new PizzaApp();

        // get a product
        var product = app.getProductService().getProduct("P-10");
        System.out.println(product);

        // get a customer
        var customer = app.getCustomerService().getCustomerByPhoneNumber("+49 123 456789");
        System.out.println(customer);

        // place an order
        var order = app.getOrderService().placeOrder(
                "+49 123 456789",
                ofEntries(
                        entry("P-10", 3),
                        entry("S-03", 1)
                )
        );
        System.out.println(order);
    }
}
