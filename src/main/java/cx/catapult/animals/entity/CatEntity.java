package cx.catapult.animals.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("cat")
public class CatEntity extends AnimalEntity {
}