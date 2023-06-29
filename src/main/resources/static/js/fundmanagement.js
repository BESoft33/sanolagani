

document.addEventListener("DOMContentLoaded", function () {
  var canvas = document.getElementById("myChart2");

  if (canvas) {
    var ctx = canvas.getContext("2d");


    var companies =[[${companynamelist}]]; // Replace with your actual company names
    console.log(companies);
    var shares = [2, 3, 4] // Replace with your actual number of shares data
    var investments = [[${totalamountlist}]]; // Replace with your additional data

    // Chart data
    var chartData = {
      labels: companies,
      datasets: [
        {
          label: "investments",
          data: investments,
          type: 'line', // Bar chart for number of shares
          backgroundColor: "#007bff",
          borderWidth: 1
        },

      ]
    };
    // Chart options
    var chartOptions = {
      responsive: true,
      scales: {
        y: {
          beginAtZero: true
        }
      }
    };

    // Create combination chart
    var myChart = new Chart(ctx, {
      type: 'line',
      data: chartData,
      options: chartOptions
    });

  }
});



  var config = {
      // replace the publicKey with yours
      "publicKey": "test_public_key_46b3052e00e145b5b2d988d17359b3a4",
      "productIdentity": "1234567890",
      "productName": "Dragon",
      "productUrl": "http://gameofthrones.wikia.com/wiki/Dragons",
      "paymentPreference": [
          "KHALTI",
          "EBANKING",
          "MOBILE_BANKING",
          "CONNECT_IPS",
          "SCT",
          ],
      "eventHandler": {
          onSuccess (payload) {
              // hit merchant api for initiating verfication
              console.log(payload);
          },
          onError (error) {
              console.log(error);
          },
          onClose () {
              console.log('widget is closing');
          }
      }
  };

  var checkout = new KhaltiCheckout(config);

  var payButton = document.getElementById("payment-button");
  payButton.addEventListener("click", function () {
       var amountInput = document.getElementById("amount-input");
      var amount = amountInput.value;
         if (amount) {
          var amountInPaisa = amount * 100; // Convert amount from rupees to paisa
          config.amount = amountInPaisa; // Set the amount in the config object
          payButton.innerHTML = "Pay " + amount + " Rupees";
          var amountInput = document.getElementById("amount");
         amountInput.value = amount;
          checkout.show({popupMobile: true});
      } else {
          alert("Please enter the amount.");
      }
  });
