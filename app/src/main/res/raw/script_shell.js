var env = {};
var max_iter = 100;
var iter = 0;
var function_stack = [];
env.execute = function(next_fun) {
    function_stack.push(next_fun);
}
function_stack.push(entryFunction);

while (function_stack.length != 0) {
    var top_fun = function_stack.pop();
    top_fun(env);
    iter++;
    if (iter > max_iter) {
        break;
    }
}

function_stack.length == 0;