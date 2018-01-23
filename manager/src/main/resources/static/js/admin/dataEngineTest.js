Vue.component('subop-template', {
  template: '#subop-template',
  props: {
    suboperationlist: {
        type: Object
    }
  }
});

var app = new Vue({
    el: '#app',

    // Define the data model. We're going to submit this to the server
    data: {
      datamodel: JSON.parse($("#listOperationsJson").text()),
      operation: {},
      topLevelSubOpType: null
    },

    // Define the methods for the model/view
    methods:
    {
        // This method is called when the Operation type is selected. It initializes the operation data with default values,
        // as well as important ui-related flags
        operationTypeChanged: function(e)
        {
            this.operation = {}
            this.operation.index = e.target.value;

            if (StringUtils.isNotBlank(this.operation.index))
            {
                // Build a stub of the operation using the datamodel
                for (var i = 0; i < this.datamodel[this.operation.index].params.length; i++)
                {
                    var param = this.datamodel[this.operation.index].params[i];

                    // Set the value to the default if one exists, null otherwise. We want this value to be "watched" for UI changes
                    var defaultValue = StringUtils.isNotBlank(param.defaultValue) ? param.defaultValue : '';
                    Vue.set(this.operation, param.key, defaultValue);

                    // Set flags for the operation. These dont need to be "watched" for UI-initiated changes
                    this.operation[param.key]
                }
            }
        },

        // This method is called when a sub-operation is added to a parent operation.
        addSubOperation: function(parentOperation, opType)
        {
            // If this is true, then we are adding a "top-level" sub-operation. In other words, this sub-op has no parent.
            if (parentOperation == null)
            {
                if (app.operation.subOperations == null)
                    Vue.set(app.operation, "subOperations",  {});

                parentOperation = this.operation;
            }
            else // Otherwise, we are adding a sub-operation to an existing sub-operation. My head....ow...
            {
                if (parentOperation.subOperations == null)
                    Vue.set(subOperation, "subOperations",  {});
            }

            var path = (parentOperation.path != null) ? parentOperation.path + "."+opType : opType;

            // Initialize the object that represents the new sub-operation
            var newSubOp = {
                key: opType,
                path: path,
                subOperations: {}
            };

            // Build a stub of the operation using the datamodel, initializing it using any default values provided
            for (var i = 0; i < this.datamodel[this.operation.index].subOperations[opType].params.length; i++)
            {
                var param = this.datamodel[this.operation.index].subOperations[opType].params[i];

                // Set the value to the default if one exists, null otherwise
                var defaultValue = StringUtils.isNotBlank(param.defaultValue) ? param.defaultValue : '';
                Vue.set(newSubOp, param.key, defaultValue);
            }

            // Explicitly tell Vue to watch for changes (without this, the model is not updated when changes occur
            Vue.set(parentOperation.subOperations,opType, newSubOp);
        },

        getAllSubOperations: function()
        {
            var allSubOperations = [];
            for (var key in this.operation.subOperations)
            {
                var subOp = this.operation.subOperations[key];
                subOp.level = 0;
                allSubOperations.push(subOp);

                this.getAllChildrenRecursive(subOp, 0, allSubOperations);

            }
            return allSubOperations;
        },

        getAllChildrenRecursive: function(subOperation, level, list)
        {
            var newLevel = level + 1;
            for (var key in subOperation.subOperations)
            {
                var subOp = subOperation.subOperations[key];
                subOp.level = newLevel;
                list.push(subOp);

                this.getAllChildrenRecursive(subOp, newLevel, list);
            }
        },

        deleteOperation: function(parentOperation, pathForDeletion)
        {
            if (parentOperation == null)
                parentOperation = this.operation;

            for (var opKey in parentOperation.subOperations)
            {
                var subop = parentOperation.subOperations[opKey];
                var subopPath = subop.path;
                if (subopPath == pathForDeletion)
                {
                    Vue.delete(parentOperation.subOperations, opKey);
                    break;
                }
                else
                {
                    this.deleteOperation(subop, pathForDeletion);
                }
            }
        }
    },

    computed: {
    },

    // Run this when Vue is ready
    mounted() {
        initBootstrapFilePickers();
    },
});