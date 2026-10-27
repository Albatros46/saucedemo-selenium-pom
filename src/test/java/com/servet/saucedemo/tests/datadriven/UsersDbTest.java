package com.servet.saucedemo.tests.datadriven;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.servet.saucedemo.base.BaseTest;
import com.servet.saucedemo.base.TestSecrets;
import com.servet.saucedemo.data.DbReader.DbUser;
import com.servet.saucedemo.data.TestDataProviders;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/** Veritabanındaki her aktif kullanıcı için: giriş yapabilmeli mi, yapamıyorsa hangi mesajı görmeli? */
@Epic("SauceDemo shop")
@Feature("Data-driven: Database")
public class UsersDbTest extends BaseTest {

    @Test(dataProvider = "usersFromDb", dataProviderClass = TestDataProviders.class,
            groups = "datadriven", description = "Kullanıcılar H2 veritabanından")
    public void userLoginBehaviourMatchesDb(DbUser user) {
        if (user.canLogin()) {
            assertEquals(openLoginPage().loginAs(user.username(), TestSecrets.resolve(user.password())).title(), "Products", user.toString());
        } else {
            assertEquals(openLoginPage().loginExpectingError(user.username(), TestSecrets.resolve(user.password())).errorMessage(),
                    user.expectedError(), user.toString());
        }
    }
}
