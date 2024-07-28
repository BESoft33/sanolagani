var fname = document.getElementById('fname').value;
var lname = document.getElementById('lname').value;
var mobile = document.getElementById('mobile').value;
var address = document.getElementById('address').value;
var password = document.getElementById('password');
var cpassword = document.getElementById('cpassword');
var submit = document.getElementById("submit");
var error = document.getElementById("message");

cpassword.addEventListener("keyup", matchPassword);


function matchPassword() {  
    error.innerText = "Enter identical password on both fields.";
    let pass = password.value;
    let cpass = cpassword.value;
    console.log(pass);
    if (pass === cpass) {
        error.innerText = "";
        submit.disabled = false;
    }
    else {
        submit.disabled = true;
    }
}

document.getElementById('signup-form').addEventListener('submit', e => {
    e.preventDefault();
    if(registerValidate()==true){
        document.getElementById('signup-form').submit();
    }
});

registerValidate = ()=>{
    let pass = password.value;
    let cpass = cpassword.value;

    console.log("inside register validate");
    let fnamecheck = /^[a-zA-Z]{3,}$/;
    let lnamecheck = /^[a-zA-Z]{3,}$/;
    let mobilecheck = /^9[678]\d{8}$/;
    let addresscheck = /^[a-zA-Z0-9\s,-]{3,}$/;
    let passwordcheck = /^(?=.*[0-9])(?=.*[!@#$%^*])[a-zA-Z0-9!@#$%^&*]{8,16}$/;

    if(fnamecheck.test(fname)){
        document.getElementById('error-msg').innerHTML = "";
    }else{
        document.getElementById('error-msg').innerHTML = "** Enter valid first name";
        return false;
    }

    if(lnamecheck.test(lname)){
        document.getElementById('error-msg').innerHTML = "";
    }else{
        document.getElementById('error-msg').innerHTML = "** Enter valid lastname";
        return false;
    }

    if(mobilecheck.test(mobile)){
        document.getElementById('error-msg').innerHTML = "";
    }else{
        document.getElementById('error-msg').innerHTML = "** Enter valid mobile number";
        return false;
    }

    if(addresscheck.test(address)){
        document.getElementById('error-msg').innerHTML = "";
    }else{
        document.getElementById('error-msg').innerHTML = "** Enter valid address";
        return false;
    }

    if(passwordcheck.test(pass)){
        document.getElementById('error-msg').innerHTML = "";
    }else{
        document.getElementById('error-msg').innerHTML = "** Enter valid password";
        return false;
    }
    if(pass.length < 8){
        document.getElementById('error-msg').innerHTML = "** Password must be atleast 8 characters";
        return false;
    }

    if(pass !== cpass){
        document.getElementById('error-msg').innerHTML = "** Password Not matched";
        return false;
    }
    console.log("success");
    return true;
};