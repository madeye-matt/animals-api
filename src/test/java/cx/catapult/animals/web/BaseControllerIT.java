package cx.catapult.animals.web;

import static org.assertj.core.api.Assertions.assertThat;

import cx.catapult.animals.domain.Animal;
import cx.catapult.animals.domain.Arachnid;
import cx.catapult.animals.domain.Group;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseControllerIT<T extends Animal> {
    public static final String DEFAULT_ANIMAL_NAME = "Default animal name";
    public static final String DEFAULT_ANIMAL_DESCRIPTION = "Default animal description";
    public static final Group DEFAULT_ANIMAL_GROUP = Group.AMPHIBIAN;

    @LocalServerPort
    private int port;

    private URL base;

    protected abstract T createInstance(final String name, final String description);
    protected abstract String getUrlSuffix();
    protected abstract int getExpectedItems();

    @Autowired
    protected TestRestTemplate template;

    @BeforeEach
    public void setUp() throws Exception {
        this.base = new URL(String.format("http://localhost:%d/api/1/%s", port, getUrlSuffix()));
    }

    @Test
    public void createShouldWork() {
        T instance = createInstance();
        ResponseEntity<? extends Animal> response = template.postForEntity(base.toString(), instance,
                                                                                   instance.getClass());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotEmpty();
        assertThat(response.getBody().getName()).isEqualTo(instance.getName());
        assertThat(response.getBody().getDescription()).isEqualTo(instance.getDescription());
        assertThat(response.getBody().getGroup()).isEqualTo(instance.getGroup());
    }

    @Test
    public void allShouldWork() {
        Collection items = template.getForObject(getUrl(), Collection.class);
        assertThat(items.size()).isGreaterThanOrEqualTo(getExpectedItems());
    }

    @Test
    public void getShouldWork() {
        T created = create("Test 1");
        ResponseEntity<String> response = template.getForEntity(getUrl(created.getId()), String.class);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    public void updateShouldWork() throws MalformedURLException {
        T instance = createInstance();
        T updatedInstance = createInstance("Updated name", "Updated desc");
        ResponseEntity<? extends Animal> postResponse = template.postForEntity(getUrl(), instance,
                                                                                                  instance.getClass());
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final String id = postResponse.getBody().getId();
        final String url = getUrl(id);
        template.put(url, updatedInstance);

        ResponseEntity<? extends Animal> response = template.getForEntity(url, updatedInstance.getClass());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(id);
        assertThat(response.getBody().getName()).isEqualTo(updatedInstance.getName());
        assertThat(response.getBody().getDescription()).isEqualTo(updatedInstance.getDescription());
    }

    T create(String name) {
        T instance = createInstance(name, name);
        T created = (T) template.postForObject(getUrl(), instance, instance.getClass());
        assertThat(created.getId()).isNotEmpty();
        assertThat(created.getName()).isEqualTo(name);
        return created;
    }

    protected T createInstance(){
        return createInstance(DEFAULT_ANIMAL_NAME, DEFAULT_ANIMAL_DESCRIPTION);
    }

    protected String getUrl(){
        return this.base.toString();
    }

    protected String getUrl(final String id){
        return String.format("%s/%s", getUrl(), id);
    }
}
