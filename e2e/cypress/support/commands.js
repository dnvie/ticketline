Cypress.Commands.add('loginAdmin', () => {
    cy.fixture('settings').then(settings => {
        cy.visit(settings.baseUrl);
        cy.contains('div', 'Sign In').click();
        cy.get('input[name="username"]').type(settings.adminUser);
        cy.get('input[name="password"]').type(settings.adminPw);
        cy.contains('button', 'Login').click();
    })
})

Cypress.Commands.add('createMessage', (msg) => {
    cy.fixture('settings').then(settings => {
        cy.contains('.navButton', 'Admin Panel').click();
        cy.contains('a', 'Create News Entry').click();
        cy.get('input[name="title"]').type('title' +  msg);
        cy.get('textarea[formControlName="summary"]').type('summary' +  msg);
        cy.get('textarea[formControlName="text"]').type('text' +  msg);
        cy.contains('div', 'Save').click();

        cy.contains('title' +  msg).should('be.visible');
        cy.contains('summary' +  msg).should('be.visible');
    })
})
