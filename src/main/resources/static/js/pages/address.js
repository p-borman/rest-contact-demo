(function () {
    var events = {
        showList: 'show:list',
        showAdd: 'show:add',
        refreshList: 'list:refresh'
    };

    var app = angular.module('addressApp', []);

    app.controller('showController', ['$rootScope', function ($rootScope) {
        var self = this;
        self.showList = true;
        self.showAdd = false;

        self.viewList = function () {
            self.showList = true;
            self.showAdd = false;
        };

        self.viewAdd = function () {
            self.showList = false;
            self.showAdd = true;
        };
        $rootScope.$on(events.showList, function (event, arg) {
            self.viewList();
        });
        $rootScope.$on(events.showAdd, function (event, arg) {
            self.viewAdd();
        });
    }]);

    app.controller('addressListController', ['addressService', '$rootScope', function (addressService, $rootScope) {
        var self = this;
        self.addresses = [];
        self.error = "";
        self.activeOnly = false;
        self.activeOnlyOnLastRequest = false;

        self.pageNumber = 0;
        self.isFirstPage = true;
        self.isLastPage = true;
        self.itemsPerPage = 25;
        self.itemsPerPageOptions = [10, 25, 50, 100];
        self.totalItems = 0;
        self.totalPages = 0;
        self.pageNumbers = [];


        self.showError = function () {
            return self.error != "";
        };

        self.getAddresses = function (pageNumber, pageSize, activeOnly) {
            self.error = "";
            self.activeOnly = activeOnly;
            addressService.getAddresses(pageNumber, pageSize, activeOnly).then(function (response) {
                    if (response.status == 200) {
                        if (response.data.error) {
                            self.error = response.data.message;
                            self.addresses = [];
                        }
                        else {
                            self.addresses = response.data.addresses.content;
                            self.pageNumber = response.data.addresses.number;
                            self.isFirstPage = response.data.addresses.first;
                            self.isLastPage = response.data.addresses.last;
                            self.itemsPerPage = response.data.addresses.size;
                            self.totalItems = response.data.addresses.totalElements;
                            self.activeOnlyOnLastRequest = activeOnly;
                            self.totalPages = response.data.addresses.totalPages;
                            self.pageNumbers = [];
                            for (var i = 0; i < self.totalPages; i++) {
                                self.pageNumbers.push(i);
                            }
                        }
                        $rootScope.$broadcast(events.showList);
                    }
                },
                function (reason) {
                    self.error = reason.status + ' - ' + reason.error + ": " + reason.message;
                    self.addresses = [];
                    $rootScope.$broadcast(events.showList);
                });
        };

        self.deleteAddress = function (address) {
            self.error = "";
            addressService.deleteAddress(address).then(function (response) {
                    if (response.status == 200) {
                        if (response.data.error) {
                            self.error = response.data.message;
                        }
                        else {
                            self.refreshList();
                        }
                    }
                },
                function (reason) {
                    self.error = reason.status + ' - ' + reason.error + ": " + reason.message;
                });
        };


        self.goToPage = function (pageNumber, activeOnly) {
            if (activeOnly != self.activeOnlyOnLastRequest) {
                self.getAddresses(0, self.itemsPerPage, activeOnly);
            }
            else if (pageNumber >= 0 && pageNumber < self.totalPages && pageNumber != self.pageNumber) {
                self.getAddresses(pageNumber, self.itemsPerPage, activeOnly);
            }
        };

        self.itemsPerPageChanged = function () {
            self.pageNumber = 0;
            self.getAddresses(self.pageNumber, self.itemsPerPage, self.activeOnly);
        };

        self.isOnPage = function (pageNumber) {
            return self.pageNumber == pageNumber;
        };

        self.getAddresses(self.pageNumber, self.itemsPerPage, self.activeOnly);

        self.refreshList = function () {
            $rootScope.$broadcast(events.refreshList)
        };

        $rootScope.$on(events.refreshList, function (event, arg) {
            self.getAddresses(self.pageNumber, self.itemsPerPage, self.activeOnly);
        });
    }]);

    app.controller('addressAddController', ['addressService', '$rootScope', function (addressService, $rootScope) {
        var self = this;
        self.street = '';
        self.street2 = '';
        self.city = '';
        self.state = '';
        self.zip = '';
        self.active = false;
        self.save = function () {
            addressService.saveAddress({
                street: self.street,
                street2: self.street2,
                city: self.city,
                state: self.state,
                zip: self.zip,
                active: self.active
            });
        };
        self.cancel = function () {
            self.street = '';
            self.street2 = '';
            self.city = '';
            self.state = '';
            self.zip = '';
            self.active = false;
            $rootScope.$emit(events.showList)
        };

        $rootScope.$on(events.refreshList, function () {
            self.cancel();
        })
    }]);

    app.service('addressService', ['$http', '$rootScope', function ($http, $rootScope) {
        var self = this;
        self.getAddresses = function (pageNumber, pageSize, activeOnly) {
            return $http.get("/addresses/" + pageNumber + "/" + pageSize + "/" + activeOnly, {headers: {'Accept': 'application/json'}});
        };
        self.deleteAddress = function (address) {
            return $http.delete("/address/" + address.id, {headers: {'Accept': 'application/json'}});
        };
        self.saveAddress = function (address) {
            $http.post("/address/", address, {headers: {'Accept': 'application/json'}})
                .then(function () {
                    $rootScope.$broadcast(events.refreshList);
                });
        };
    }]);
})();