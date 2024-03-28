export class HunterViewPage {
  private readonly baseUrl = '/';
  private readonly huntTitle = '.hunt-title';
  private readonly huntNofTasksEst = '.hunt';
  private readonly huntTaskList = '.task-list';
  private readonly hunterUploadImage = '.image-upload';
  private readonly HunterButton = '[name="hunter-button"]';
  private readonly HostButton = '[name="host-button"]';
  private readonly hostCardSelector = '.hunt-cards-container app-hunt-card';
  private readonly profileButtonSelector = '[data-test=viewProfileButton]';
  private readonly beginHuntButton = '.begin-hunt';
  private readonly huntAccessCode = '.col-md-12 h1';
  private readonly UploadImageButton = '.image-upload input[type="file"]';

  navigateTo() {
    return cy.visit(this.baseUrl);
  }

  /**
   * Get the title of the hunter view page.
   *
   * @return the value of the element with the ID `.hunt-title`
   */
  getHunterViewTitle() {
    return cy.get(this.huntTitle);
  }

  /**
   * Get the hunter button DOM element.
   *
   * @return the value of the element with the ID `[name="hunter-button"]`
   */
  getHunterButton() {
    return cy.get(this.HunterButton);
  }

  /**
   * Get the host button DOM element.
   *
   * @return the value of the element with the ID `[name="host-button"]`
   */
  getHostButton() {
    return cy.get(this.HostButton);
  }

  /**
   * Get all the `app-host-card` DOM elements. This will be
   * empty if we're using the list view of the hosts.
   *
   * @returns an iterable (`Cypress.Chainable`) containing all
   *   the `app-host-card` DOM elements.
   */
  getHuntCards() {
    return cy.get(this.hostCardSelector);
  }

  /**
   * Clicks the "view profile" button for the given host card.
   * Requires being in the "card" view.
   *
   * @param card The host card
   */
  clickViewProfile(card: Cypress.Chainable<JQuery<HTMLElement>>) {
    return card.find<HTMLButtonElement>(this.profileButtonSelector).click();
  }

  /**
   * Clicks the "begin hunt" button in the hunt page.
   * Requires being in the "hunt" view as hosts.
   *
   * @returns the value of the element with the ID `.begin-hunt`
   */
  clickBeginHunt() {
    return cy.get(this.beginHuntButton).click();
  }

  /**
   * Get the access code from the started Hunt.
   * Requires begin Hunt in the "hunt" view as hosts.
   *
   * @returns the value of the element with the class `.col-md-12 Access Code`
   */
  getAccessCode() {
    return cy.get(this.huntAccessCode).invoke('text').as('accessCode');
  }

  /**
   * Get the number of tasks and estimated time of the hunt as hunter-view.
   *
   * @returns the value of the element with the class `.hunt`
   */
  getHuntNofTasksEst() {
    return cy.get(this.huntNofTasksEst);
  }

  /**
   * Get the task list of the hunt as hunter-view.
   *
   * @return the value of the element with the class `.task-list`
   */
  getHuntTaskList() {
    return cy.get(this.huntTaskList);
  }

  /**
   * Get the upload image button and the image input field.
   *
   * @return the value of the element with the class `.image-upload`
   */
  getHunterUploadImage() {
    return cy.get(this.hunterUploadImage);
  }

  /**
   * Clicks the "upload image" button in the hunter view table.
   *
   * @return the value of the element with the class `.image-upload input[type="file"]`
   */
  clickUploadImage() {
    return cy.get(this.UploadImageButton).first().click({force: true});
  }
}
