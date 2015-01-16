//xx execute the code
var env = {};
var function_stack = [];
env.execute = function(next_fun) {
    function_stack.push(next_fun);
}
function_stack.push(entryFunction);

while (function_stack.length != 0) {
    var top_fun = function_stack.pop();
    top_fun(env);
}