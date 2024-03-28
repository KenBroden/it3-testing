import { HunterViewPage } from "cypress/support/hunter-view.po";

const page = new HunterViewPage();

describe('Hunter View', () => {
  beforeEach(() => page.navigateTo());

  /**
   * These test will get to the hunt, begin it and capture the access code.
   * Then it will navigate to the hunter view page with the access code.
   */

  it('should navigate to the right hunter view page with the captured access code', () => {
    page.getHostButton().click();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    page.getAccessCode();

    // Those above will navigate to the Hunt, begin it
    // and capture the access code.

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunter-view/${accessCode}`);
    });
  });

  it('should display the hunt title as hunter-view', () => {
    page.getHostButton().click();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    page.getAccessCode();

    // Those above will navigate to the Hunt, begin it
    // and capture the access code.

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunter-view/${accessCode}`);
    });

    // navigate to the hunter view page with access code.

    page.getHunterViewTitle().contains('You are in');
  });

  it('should display the hunt number of tasks and estimate time', () => {
    page.getHostButton().click();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    page.getAccessCode();

    // Those above will navigate to the Hunt, begin it
    // and capture the access code.

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunter-view/${accessCode}`);
    });

    // navigate to the hunter view page with access code.

    page.getHuntNofTasksEst().contains('min');
    page.getHuntNofTasksEst().contains('tasks');
  });

  it('should display the hunt tasks list in Your Task column', () => {
    page.getHostButton().click();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    page.getAccessCode();

    // Those above will navigate to the Hunt, begin it
    // and capture the access code.

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunter-view/${accessCode}`);
    });

    // navigate to the hunter view page with access code.

    page.getHuntTaskList().should('exist');
  });

  it('should display the upload picture button and the picture input field', () => {
    page.getHostButton().click();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    page.getAccessCode();

    // Those above will navigate to the Hunt, begin it
    // and capture the access code.

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunter-view/${accessCode}`);
    });

    // navigate to the hunter view page with access code.

    page.getHunterUploadImage().should('exist');
  });

  it('should click the Upload Image button', () => {
    page.getHostButton().click();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    page.getAccessCode();

    // Those above will navigate to the Hunt, begin it
    // and capture the access code.

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunter-view/${accessCode}`);
    });

    // navigate to the hunter view page with access code.

    page.clickUploadImage();
  })
});
