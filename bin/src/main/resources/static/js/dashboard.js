        document.addEventListener("DOMContentLoaded", function () {
          var canvas = document.getElementById("myChart2");
          console.log("Canvas", canvas);
          if (canvas) {
            var ctx = canvas.getContext("2d");

            var companies =[[${companynamelist}]]; // Replace with your actual company names
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
