package scratch;

import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactoryBean;
import pizza.customer.Customer;
import pizza.customer.CustomerService;
import pizza.product.HashMapProductRepository;
import pizza.product.ProductRepository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AopDemo {

    public static void main(String[] args) {
        // create aspect
        var security = new Security();
        security.loggedInUsername = "tom";
        var aspect = new SecurityCheckAspect(security);

        // generate AOP proxy for service
        var custSrv = new CustomerService$Proxy(aspect);
        custSrv.createCustomer(new Customer("Tom Test", null, null));

        // generate AOP proxy for repository
        var prodRepo = new HashMapProductRepository();
        ProductRepository prodRepoProxy = (ProductRepository) Proxy.newProxyInstance(
                prodRepo.getClass().getClassLoader(),
                new Class<?>[]{ProductRepository.class},
                new SecurityInvocationHandler(aspect, prodRepo)
        );
        prodRepoProxy.findAll();


        // SecurityCheckAspect via spring way
        var factoryBean = new ProxyFactoryBean();
        factoryBean.setTarget(new CustomerService());
        factoryBean.addAdvice(new SecurityCheckAspect(security));
        CustomerService proxiedCustomerService = (CustomerService) factoryBean.getObject();
        proxiedCustomerService.getAllCustomers();
    }
}


// this is usually generated by CGLIB
class CustomerService$Proxy extends CustomerService {

    private final SecurityCheckAspect aspect;

    CustomerService$Proxy(SecurityCheckAspect aspect) {
        this.aspect = aspect;
    }

    @Override
    public Iterable<Customer> getAllCustomers() {
        this.aspect.checkIsUserLoggedIn();
        return super.getAllCustomers();
    }
}


class SecurityInvocationHandler implements InvocationHandler {

    private final SecurityCheckAspect aspect;
    private final ProductRepository originalBean;

    SecurityInvocationHandler(SecurityCheckAspect aspect, ProductRepository originalBean) {
        this.aspect = aspect;
        this.originalBean = originalBean;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        this.aspect.checkIsUserLoggedIn();
        return method.invoke(originalBean, args);
    }
}


class SecurityCheckAspect implements MethodBeforeAdvice {

    // some other business logic bean required by this aspect
    private final Security security;

    public SecurityCheckAspect(Security security) {
        this.security = security;
    }

    // this is the aspect's core logic
    public void checkIsUserLoggedIn() {
        if (!this.security.isUserLoggedIn()) {
            throw new IllegalStateException("Not authorized!!");
        }
    }

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        // call aspect
        checkIsUserLoggedIn();
    }
}


class Security {

    public String loggedInUsername = null;

    public boolean isUserLoggedIn() {
        return loggedInUsername != null;
    }

}
