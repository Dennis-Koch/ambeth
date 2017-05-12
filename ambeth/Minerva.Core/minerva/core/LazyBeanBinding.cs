using System;
using System.Linq.Expressions;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using System.Dynamic;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Minerva.Core;
using System.Collections.Generic;
using Expression = System.Linq.Expressions.Expression;
using System.ComponentModel;

namespace De.Osthus.Minerva.Core
{
    public class LazyBeanBinding : DynamicObject
    {
        //public FrameworkElement Parent { get { return parent; } }

        public String Name { get { return name; } }

        //protected FrameworkElement parent;

        protected String name;

        public virtual bool IsBusy { get; set; }

        public LazyBeanBinding(String name)
        {
            //this.parent = parent;
            this.name = name;
            IsBusy = true;
        }

        public virtual void HandleLookup()
        {
            throw new Exception("Xaml Binding lookup too early.");
        }
        public override IEnumerable<string> GetDynamicMemberNames()
        {
            HandleLookup();
            return new List<string>();
        }
        public override DynamicMetaObject GetMetaObject(Expression parameter)
        {
            HandleLookup();
            return null;
        }
        public override bool TryBinaryOperation(BinaryOperationBinder binder, object arg, out object result)
        {
            HandleLookup();
            result = null;
            return false;
        }
        public override bool TryConvert(ConvertBinder binder, out object result)
        {
            HandleLookup();
            result = null;
            return false;
        }
        public override bool TryCreateInstance(CreateInstanceBinder binder, object[] args, out object result)
        {
            HandleLookup();
            result = null;
            return false;
        }
        public override bool TryDeleteIndex(DeleteIndexBinder binder, object[] indexes)
        {
            HandleLookup();
            return false;
        }
        public override bool TryDeleteMember(DeleteMemberBinder binder)
        {
            HandleLookup();
            return false;
        }
        public override bool TryGetIndex(GetIndexBinder binder, object[] indexes, out object result)
        {
            HandleLookup();
            result = null;
            return false;
        }
        public override bool TryGetMember(GetMemberBinder binder, out object result)
        {
            HandleLookup();
            result = null;
            return false;
        }
        public override bool TryInvoke(InvokeBinder binder, object[] args, out object result)
        {
            HandleLookup();
            result = null;
            return false;
        }
        public override bool TryInvokeMember(InvokeMemberBinder binder, object[] args, out object result)
        {
            HandleLookup();
            result = null;
            return false;
        }
        public override bool TrySetIndex(SetIndexBinder binder, object[] indexes, object value)
        {
            HandleLookup();
            return false;
        }
        public override bool TrySetMember(SetMemberBinder binder, object value)
        {
            HandleLookup();
            return false;
        }
        public override bool TryUnaryOperation(UnaryOperationBinder binder, out object result)
        {
            HandleLookup();
            result = null;
            return false;
        }
        ///TODO: We may return a hierarchy of DynObjs
    }
}
