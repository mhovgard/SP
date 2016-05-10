/* jshint -W117, -W030 */
describe('resetController', function() {
    var controller;

    beforeEach(function() {
        bard.appModule('app.resetWidget');
        bard.inject('$controller', '$log', '$rootScope', '$state');
        $state.current = {title: 'resetWidget'};
    });

    beforeEach(function () {
        controller = $controller('resetController');
        $rootScope.$apply();
    });

    bard.verifyNoOutstandingHttpRequests();

    describe('reset controller', function() {
        it('should be created successfully', function () {
            expect(controller).to.be.defined;
        });

        describe('after activate', function() {
            it('should have empty log', function() {
                expect(controller.eventLog).to.equal([]);
            });

            it('should have logged "Activated"', function() {
                expect($log.info.logs).to.match(/Activated/);
            });
        });
    });
});
