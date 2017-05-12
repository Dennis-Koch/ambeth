using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;

namespace De.Osthus.Minerva.Dialogs
{
    public partial class LoginControl : System.Windows.Controls.UserControl
    {
        #region Events

        public event EventHandler Closed;

        #endregion

        #region Properties

        public string UserName { get; set; }

        public string Password { get; set; }

        #endregion

        #region Constructor

        public LoginControl()
        {
            InitializeComponent();
        }

        #endregion

        #region Private methods

        public void OnLoginClick()
        {
            if (!string.IsNullOrEmpty(textBoxUsername.Text) && !string.IsNullOrEmpty(textBoxPassword.Password))
            {
                UserName = textBoxUsername.Text;
                Password = textBoxPassword.Password;
                if (Closed != null)
                {
                    Closed(this, new EventArgs());
                }
            }
        }

        #endregion

        #region Event handler

        private void btnLogin_Click(object sender, RoutedEventArgs e)
        {
            OnLoginClick();
        }

        private void UserControl_Loaded(object sender, RoutedEventArgs e)
        {
            System.Windows.Browser.HtmlPage.Plugin.Focus(); // without this "textBoxUsername.Focus()" does not work
            textBoxUsername.Focus();
        }

        private void textBox_KeyUp(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Enter)
            {
                OnLoginClick();
            }
        }

        #endregion
    }
}
