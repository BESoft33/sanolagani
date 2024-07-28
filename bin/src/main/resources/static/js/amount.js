let amountBtn = document.getElementsByClassName("amount");
let paymentAmount = document.querySelector("#paymentAmount");
let amtElement = document.querySelector("#amount")
let feesElement = document.querySelector("#fees");
let totalAmountElement = document.querySelector("#total");
let unitsElement = document.querySelector("#units");
let confirmBtn = document.querySelector("#confirm");
let submitBtn = document.getElementById("submit");
let totalAmountPayable = document.getElementById("totalAmount");


let minInvestment = parseFloat(document.getElementById("pps").innerText.replaceAll(" ", ""));
let maxInvestment = parseFloat(document.getElementById("pps").innerText.replaceAll(" ", "")) * parseFloat(document.getElementById("maxq").innerText.replaceAll(" ", ""));
let amt = 0; //total amounts as per price per unit
let fees = 0; //brokerage charge
let totalAmount = 0; //total charge
let units = 0; //number of units


for (let i = 0; i < amountBtn.length; i++) {
    amountBtn[i].addEventListener("click", function () {
        submitBtn.disabled = true;
        let amount = amountBtn[i].innerText.replace("Rs.", "").replaceAll(",", "").replaceAll(" ", "");
        updateDetails(amount);
        amt = parseFloat(amount);
        fees = 0.02 * amt;
        paymentAmount.value = amount;
    });
}

function updateDetails(budget) {
    amt = parseFloat(budget);
    fees = Math.round(0.02 * amt, 2);
    totalAmount = amt + fees;

    amtElement.innerText = amt;
    feesElement.innerText = fees;
    totalAmountElement.innerText = totalAmount;
    totalAmountPayable.value = totalAmount;
    unitsElement.innerText = "";
}


paymentAmount.addEventListener("keyup", function(){
        submitBtn.disabled = true;
        var input = document.getElementById("paymentAmount");
        var value = input.value;
        
        if (value <= maxInvestment) {
            // Perform the desired action
            input.value=value;
            console.log(input);
        } else {
            // Value exceeds the maximum, show an error message or handle accordingly
            console.log(maxInvestment);
            input.value=maxInvestment;
        }
        var value = paymentAmount.value.replaceAll(" ","");
        updateDetails(value);
        let message = document.getElementById("message");
        message.innerText = (parseFloat(value) < minInvestment || parseFloat(value) > maxInvestment)? "Amount should be between "+ minInvestment + " and "+ maxInvestment : "";
    });

paymentAmount.addEventListener("keyup", function () {
    submitBtn.disabled = true;
    var value = paymentAmount.value.replaceAll(" ", "");
    updateDetails(value);
    let message = document.getElementById("message");
    message.innerText = (parseFloat(value) < minInvestment || parseFloat(value) > maxInvestment) ? "Amount should be between " + minInvestment + " and " + maxInvestment : "";
}

);

confirmBtn.addEventListener("click", function () {
    units = Math.floor(amt / minInvestment);
    amount = units * minInvestment;
    updateDetails(amount);
    unitsElement.innerText = units;
    submitBtn.disabled = false;
});