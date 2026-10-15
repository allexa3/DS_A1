package ro.tuc.ds2020.dtos.validators;

import ro.tuc.ds2020.dtos.validators.annotation.AgeLimit;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AgeValidator implements ConstraintValidator<AgeLimit, Integer> {

    private int ageLimit;

    @Override
    public void initialize(AgeLimit constraintAnnotation) {
        this.ageLimit = constraintAnnotation.limit();
    }

    @Override
    public boolean isValid(Integer inputAge, ConstraintValidatorContext constraintValidatorContext) {
        return inputAge == null || inputAge > ageLimit;
    }


}
